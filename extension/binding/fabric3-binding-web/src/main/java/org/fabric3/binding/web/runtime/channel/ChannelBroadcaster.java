/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.web.runtime.channel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.BroadcasterFuture;
import org.atmosphere.cpr.DefaultBroadcaster;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

import static org.fabric3.binding.web.runtime.common.ContentTypes.APPLICATION_XHTML_XML;
import static org.fabric3.binding.web.runtime.common.ContentTypes.APPLICATION_XML;
import static org.fabric3.binding.web.runtime.common.ContentTypes.TEXT_XML;

/**
 * A synchronous <code>Broadcaster</code> implementation. This class overrides the default Atmosphere asynchronous broadcast behavior as channels
 * dispatch events to consumers asynchronously.
 * <p/>
 * This implementation transforms events from a Java type to a String using either a JSON transformer (the default) or an XML transformer according to
 * the request accept header used to initiate the websocket or comet subscription. In the case of an XML transform, the broadcaster will dynamically
 * request a transformer. This is necessary as the event type is not known statically and is required to create a JAXB context. To avoid creating XML
 * transformers for every event, transformers are cached by event type. Care must therefore be taken to dispose of ChannelBroadcaster instances when
 * an application is undeployed. Otherwise, lingering JAXB contexts containing references to application classes and classloaders may inhibit garbage
 * collection.
 * <p/>
 * An additional performance optimization is made: string events that are contained in an EventWrapper are written directly to clients. This avoids
 * deserialization and re-serialization when one client publishes an event, the event is flowed through a channel, and other clients are notified via
 * the broadcaster. In this case, the serialized string representation is simply passed through without an intervening de-serialization.
 *
 * @version $Rev$ $Date$
 */
public class ChannelBroadcaster extends DefaultBroadcaster {
    private static final XSDType XSD_ANY = new XSDType(String.class, new QName(XSDType.XSD_NS, "anyType"));

    private Transformer<Object, String> jsonTransformer;
    private TransformerRegistry registry;
    private Map<Class<?>, Transformer<?, ?>> cached = new ConcurrentHashMap<Class<?>, Transformer<?, ?>>();

    public ChannelBroadcaster(String path, Transformer<Object, String> jsonTransformer, TransformerRegistry registry) {
        super(path);
        this.jsonTransformer = jsonTransformer;
        this.registry = registry;
    }

    @Override
    @SuppressWarnings({"unchecked"})
    protected void broadcast(AtmosphereResource<?, ?> resource, AtmosphereResourceEvent event) {
        serialize((AtmosphereResource<HttpServletRequest, HttpServletResponse>) resource);
        super.broadcast(resource, event);
    }

    private void serialize(AtmosphereResource<HttpServletRequest, HttpServletResponse> resource) {
        AtmosphereResourceEvent resourceEvent = resource.getAtmosphereResourceEvent();
        Object event = resourceEvent.getMessage();
        if (event instanceof EventWrapper) {
            // the event is already serialized so it can be sent directly to the client
            // TODO check that content-type and acceptType values are compatible (e.g. both JSON or both XML)
            event = ((EventWrapper) event).getEvent();
            if (!(event instanceof String)) {
                // should not happen
                throw new AssertionError("Expected a String to be passed from transport");
            }
            resourceEvent.setMessage(event);
        } else {
            try {
                String acceptType = resource.getRequest().getHeader("Accept");
                boolean isXML = isXML(acceptType);
                event = unwrap(event);
                Object transformed;
                if (isXML) {
                    Transformer<Object, String> transformer = getXmlTransformer(event.getClass());
                    transformed = transformer.transform(event, event.getClass().getClassLoader());
                } else {
                    // default to JSON
                    transformed = jsonTransformer.transform(event, event.getClass().getClassLoader());
                }
                resourceEvent.setMessage(transformed);
            } catch (TransformationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    @Override
    public Future<Object> broadcast(Object msg) {
        msg = filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, null, future));
        return cast(future);
    }

    @Override
    public Future<Object> broadcast(Object msg, AtmosphereResource r) {
        msg = filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, r, future));
        return cast(future);
    }

    @Override
    public Future<Object> broadcast(Object msg, Set<AtmosphereResource<?, ?>> subset) {
        msg = filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg);
        future.done();
        push(new Entry(msg, subset, future));
        return cast(future);
    }

    @Override
    public Future<Object> delayBroadcast(final Object o, long delay, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<?> scheduleFixedBroadcast(final Object o, long period, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    private Object unwrap(Object event) {
        if (event.getClass().isArray()) {
            Object[] wrapper = (Object[]) event;
            if (wrapper.length != 1) {
                throw new ServiceRuntimeException("Invalid event array length: " + wrapper.length);
            }
            event = wrapper[0];
        }
        return event;
    }

    private boolean isXML(String acceptType) {
        if (acceptType == null) {
            return false;
        }
        String[] types = acceptType.split(",");
        for (String type : types) {
            if (type.contains(APPLICATION_XML) || type.contains(APPLICATION_XHTML_XML) || type.contains(TEXT_XML)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private Transformer<Object, String> getXmlTransformer(Class<?> type) throws TransformationException {
        Transformer<?, ?> transformer = cached.get(type);
        if (transformer != null) {
            return (Transformer<Object, String>) transformer;
        }
        JavaClass javaType = new JavaClass(type);
        List<Class<?>> list = Collections.<Class<?>>singletonList(type);
        transformer = registry.getTransformer(javaType, XSD_ANY, list, list);
        if (transformer == null) {
            throw new TransformationException("No transformer: " + type.getClass());
        }
        cached.put(type, transformer);
        return (Transformer<Object, String>) transformer;
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }


}

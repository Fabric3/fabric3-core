/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.binding.web.runtime.common;

import java.util.Set;
import java.util.concurrent.Future;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.util.SimpleBroadcaster;
import org.fabric3.spi.container.channel.EventWrapper;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A synchronous <code>Broadcaster</code> implementation. This class overrides the default Atmosphere asynchronous broadcast behavior as channels dispatch
 * events to consumers asynchronously.
 * <p/>
 * This implementation transforms events from a Java type to a String using a JSON transformer.
 * <p/>
 * A performance optimization is made: string events that are contained in an EventWrapper are written directly to clients. This avoids deserialization and
 * re-serialization when one client publishes an event, the event is flowed through a channel, and other clients are notified via the broadcaster. In this case,
 * the serialized string representation is simply passed through without an intervening de-serialization.
 */
public class ChannelBroadcaster extends SimpleBroadcaster {
    private Transformer<Object, String> jsonTransformer;

    public ChannelBroadcaster(String path, Transformer<Object, String> jsonTransformer, AtmosphereConfig config) {
        super(path, config);
        this.jsonTransformer = jsonTransformer;
    }

    public <T> Future<T> broadcast(T msg) {
        Object transformed = serialize(msg);
        return (Future<T>) super.broadcast(transformed);
    }

    @Override
    public <T> Future<T> broadcast(T msg, AtmosphereResource r) {
        Object transformed = serialize(msg);
        return (Future<T>) super.broadcast(transformed, r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> broadcast(T msg, Set<AtmosphereResource> subset) {
        Object transformed = serialize(msg);
        return (Future<T>) super.broadcast(transformed, subset);
    }

    private void serialize(AtmosphereResource resource) {
        AtmosphereResourceEvent resourceEvent = resource.getAtmosphereResourceEvent();
        Object event = resourceEvent.getMessage();
        Object transformed = serialize(event);
        resourceEvent.setMessage(transformed);
    }

    private Object serialize(Object event) {
        if (event instanceof EventWrapper) {
            // the event is already serialized so it can be sent directly to the client
            // TODO check that content-type and acceptType values are compatible (e.g. both JSON or both XML)
            event = ((EventWrapper) event).getEvent();
            if (!(event instanceof String)) {
                // should not happen
                throw new AssertionError("Expected a String to be passed from transport");
            }
            return event;
        } else {
            try {
                event = unwrap(event);
                return jsonTransformer.transform(event, event.getClass().getClassLoader());
            } catch (TransformationException e) {
                throw new ServiceRuntimeException(e);
            }
        }
    }

    //    @SuppressWarnings({"unchecked"})
    //    public <T> Future<T> broadcast(T msg) {
    //        msg = (T)filter(msg);
    //        if (msg == null) {
    //            return null;
    //        }
    //        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg, this);
    //        future.done();
    //        push(new Entry(msg, null, future, true));
    //        return cast(future);
    //    }
    //
    //    @SuppressWarnings({"unchecked"})
    //    public <T> Future<T> broadcast(T msg, AtmosphereResource r) {
    //        msg = (T)filter(msg);
    //        if (msg == null) {
    //            return null;
    //        }
    //        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg, this);
    //        future.done();
    //        push(new Entry(msg, r, future, true));
    //        return cast(future);
    //    }
    //
    //    @SuppressWarnings({"unchecked"})
    //    public <T> Future<T> broadcast(T msg, Set<AtmosphereResource> subset) {
    //        msg = (T)filter(msg);
    //        if (msg == null) {
    //            return null;
    //        }
    //        BroadcasterFuture<?> future = new BroadcasterFuture<Object>(msg, this);
    //        future.done();
    //        push(new Entry(msg, subset, future, true));
    //        return cast(future);
    //    }
    //
    //    public <T> Future<T> delayBroadcast(T o, long delay, TimeUnit t) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public Future<?> scheduleFixedBroadcast(Object o, long period, TimeUnit t) {
    //        throw new UnsupportedOperationException();
    //    }

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

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }

}

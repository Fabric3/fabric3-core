/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.binding.web.runtime.common;

import java.util.Set;
import java.util.concurrent.Future;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.util.SimpleBroadcaster;
import org.fabric3.binding.web.runtime.channel.EventWrapper;
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
@SuppressWarnings("unchecked")
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

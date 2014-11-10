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
import java.util.concurrent.TimeUnit;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.BroadcasterFuture;
import org.atmosphere.cpr.DefaultBroadcaster;

/**
 *
 */
public class ServiceBroadcaster extends DefaultBroadcaster {

    public ServiceBroadcaster(String path, AtmosphereConfig config) {
        super(path, config);
    }

    @SuppressWarnings({"unchecked"})
    public <T> Future<T> broadcast(T msg) {
        msg = (T) filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<T> future = new BroadcasterFuture<>(msg, this);
        future.done();
        push(new Entry(msg, null, future, true));
        return cast(future);
    }

    @SuppressWarnings({"unchecked"})
    public <T> Future<T> broadcast(T msg, AtmosphereResource r) {
        msg = (T) filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg, this);
        future.done();
        push(new Entry(msg, r, future, true));
        return cast(future);
    }

    @SuppressWarnings({"unchecked"})
    public <T> Future<T> broadcast(T msg, Set<AtmosphereResource> subset) {
        msg = (T) filter(msg);
        if (msg == null) {
            return null;
        }
        BroadcasterFuture<Object> future = new BroadcasterFuture<Object>(msg, this);
        future.done();
        push(new Entry(msg, subset, future, true));
        return cast(future);
    }

    public <T> Future<T> delayBroadcast(T o, long delay, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    public Future<?> scheduleFixedBroadcast(Object o, long period, TimeUnit t) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }

}

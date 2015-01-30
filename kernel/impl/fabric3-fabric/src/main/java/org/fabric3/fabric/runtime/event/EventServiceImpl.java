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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.runtime.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3Event;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Default implementation of the EventService.
 */
@EagerInit
public class EventServiceImpl implements EventService {
    private Map<Class<Fabric3Event>, List<Fabric3EventListener<Fabric3Event>>> cache;

    public EventServiceImpl() {
        cache = new ConcurrentHashMap<>();
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void publish(Fabric3Event event) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Fabric3EventListener<Fabric3Event> listener : listeners) {
            listener.onEvent(event);
        }
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    public <T extends Fabric3Event> void subscribe(Class<T> type, Fabric3EventListener<T> listener) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(type);
        if (listeners == null) {
            listeners = new ArrayList<>();
            cache.put((Class<Fabric3Event>) type, listeners);
        }
        listeners.add((Fabric3EventListener<Fabric3Event>) listener);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public <T extends Fabric3Event> void unsubscribe(Class<T> type, Fabric3EventListener<T> listener) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(type);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }
}

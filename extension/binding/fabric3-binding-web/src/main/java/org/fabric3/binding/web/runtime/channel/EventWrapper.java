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
package org.fabric3.binding.web.runtime.channel;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Wraps an event so that it can be materialized in different representations prior to being passed to consumers.
 * <p/>
 * This class is used when reading an event from a binding and flowing it through a channel. In this case, the channel may not be aware of the event
 * type and, consequently, the event cannot be deserialized until it reaches a consumer where the type can be introspected.
 * <p/>
 * When an event is passed from a binding to a channel, the binding may opt to materialize it using a default type such as a string. When the wrapper
 * is passed to a consumer handler, the invoker may convert the default form to a specific type. As an optimization, a consumer handler may cache the
 * converted type in the wrapper to avoid conversions to the same type by subsequent handlers.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class EventWrapper implements Serializable {
    private static final long serialVersionUID = 7377714429939143568L;

    private DataType defaultType;
    private final Object defaultEvent;
    private Map<DataType, Object> cache;

    public EventWrapper(DataType type, Object event) {
        this.defaultType = type;
        this.defaultEvent = event;
    }

    public DataType getType() {
        return defaultType;
    }

    public Object getEvent() {
        return defaultEvent;
    }

    public <T> void cache(DataType type, T representation) {
        synchronized (defaultEvent) {
            if (cache == null) {
                cache = new ConcurrentHashMap<>();
                cache.put(type, defaultEvent);
            }
        }
        cache.put(type, representation);
    }

    public Object getEvent(DataType type) {
        if (cache != null) {
            return cache.get(type);
        }
        if (defaultType.equals(type)) {
            return type;
        }
        return null;
    }

}
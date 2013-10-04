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
package org.fabric3.spi.container.channel;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.model.type.contract.DataType;

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
public class EventWrapper implements Serializable {
    private static final long serialVersionUID = 7377714429939143568L;

    private DataType<?> defaultType;
    private final Object defaultEvent;
    private Map<DataType<?>, Object> cache;

    public EventWrapper(DataType<?> type, Object event) {
        this.defaultType = type;
        this.defaultEvent = event;
    }

    public DataType<?> getType() {
        return defaultType;
    }

    public Object getEvent() {
        return defaultEvent;
    }

    public <T> void cache(DataType<T> type, T representation) {
        synchronized (defaultEvent) {
            if (cache == null) {
                cache = new ConcurrentHashMap<DataType<?>, Object>();
                cache.put(type, defaultEvent);
            }
        }
        cache.put(type, representation);
    }

    public Object getEvent(DataType<?> type) {
        if (cache != null) {
            return cache.get(type);
        }
        if (defaultType.equals(type)) {
            return type;
        }
        return null;
    }

}
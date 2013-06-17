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
package org.fabric3.fabric.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3Event;
import org.fabric3.spi.event.Fabric3EventListener;

/**
 * Default implementation of the EventService.
 */
@EagerInit
public class EventServiceImpl implements EventService {
    private Map<Class<Fabric3Event>, List<Fabric3EventListener<Fabric3Event>>> cache;

    public EventServiceImpl() {
        cache = new ConcurrentHashMap<Class<Fabric3Event>, List<Fabric3EventListener<Fabric3Event>>>();
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
            listeners = new ArrayList<Fabric3EventListener<Fabric3Event>>();
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

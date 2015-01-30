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
package org.fabric3.monitor.impl.destination;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.event.MonitorEventEntry;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeDestroyed;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class MonitorDestinationRegistryImpl implements MonitorDestinationRegistry {
    private AtomicReference<MonitorDestination[]> destinations;

    private EventService eventService;

    public MonitorDestinationRegistryImpl(@Reference EventService eventService) {
        this.eventService = eventService;
    }

    @Init
    public void init() throws IOException {
        destinations = new AtomicReference<>();
        destinations.set(new MonitorDestination[0]);
        eventService.subscribe(RuntimeDestroyed.class, new MonitorEventListener());
    }

    public synchronized void register(MonitorDestination destination) {
        MonitorDestination[] source = destinations.get();
        MonitorDestination[] target = new MonitorDestination[source.length + 1];
        System.arraycopy(source, 0, target, 0, source.length);
        target[target.length - 1] = destination;
        destinations.lazySet(target);
    }

    public MonitorDestination unregister(String name) {
        throw new UnsupportedOperationException();
    }

    public int getIndex(String name) {
        MonitorDestination[] copy = destinations.get();
        for (int i = 0; i < copy.length; i++) {
            MonitorDestination destination = copy[i];
            if (destination.getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void write(MonitorEventEntry entry) throws IOException {
        int index = entry.getDestinationIndex();
        checkIndex(index);
        destinations.get()[index].write(entry);
    }

    public void write(int index, MonitorLevel level, long timestamp, String source, String template, Object... args) throws IOException {
        checkIndex(index);
        destinations.get()[index].write(level, timestamp, source, template, args);
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= destinations.get().length) {
            throw new AssertionError("Invalid index: " + index);
        }
    }

    private void stop() throws IOException {
        MonitorDestination[] copy = destinations.get();
        for (MonitorDestination destination : copy) {
            destination.stop();
        }
    }

    private class MonitorEventListener implements Fabric3EventListener<RuntimeDestroyed> {

        public void onEvent(RuntimeDestroyed event) {
            try {
                // Use RuntimeDestroy since destinations must be stopped after all other system components have been stopped during the RuntimeStopped event
                stop();
            } catch (IOException e) {
                // cannot log - send to stdout
                e.printStackTrace();
            }
        }
    }

}

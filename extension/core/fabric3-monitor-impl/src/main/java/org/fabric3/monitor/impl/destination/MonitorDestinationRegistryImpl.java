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
package org.fabric3.monitor.impl.destination;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.appender.AppenderCreationException;
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
    public void init() throws IOException, AppenderCreationException {
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

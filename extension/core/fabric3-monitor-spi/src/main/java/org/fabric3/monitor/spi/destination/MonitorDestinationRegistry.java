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
package org.fabric3.monitor.spi.destination;

import java.io.IOException;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Manages and dispatches to {@link MonitorDestination}s.
 */
public interface MonitorDestinationRegistry {

    /**
     * Registers a {@link MonitorDestination}.
     *
     * @param destination the destination
     */
    void register(MonitorDestination destination);

    /**
     * Un-registers a monitor destination corresponding to the given name.
     *
     * @param name the destination name
     * @return the un-registered name
     */
    MonitorDestination unregister(String name);

    /**
     * Returns the index for the destination corresponding to the given name.
     *
     * @param name the destination name
     * @return the index
     */
    int getIndex(String name);

    /**
     * Dispatches the entry to a destination.
     *
     * @param entry the entry
     * @throws IOException if there is a dispatch error
     */
    void write(MonitorEventEntry entry) throws IOException;

    /**
     * Dispatches event data to a destination.
     *
     * @param index     the destination index
     * @param level     the monitor level to write
     * @param timestamp the timestamp
     * @param source    the event source
     * @param args      the arguments
     * @throws IOException if there is a dispatch error
     */
    void write(int index, MonitorLevel level, long timestamp, String source, String template, Object... args) throws IOException;

}

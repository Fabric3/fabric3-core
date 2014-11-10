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
 * A destination for monitor events.
 */
public interface MonitorDestination {

    /**
     * The unique destination name.
     *
     * @return the name
     */
    String getName();

    /**
     * Initializes the destination to record events.
     *
     * @throws IOException if an initialization error occurs
     */
    void start() throws IOException;

    /**
     * Closes any open resources used by the destination.
     *
     * @throws IOException if an exception occurs closing resources
     */
    void stop() throws IOException;

    /**
     * Writes the entry to the destination.
     *
     * @param entry the entry
     * @throws IOException if there is a dispatch error
     */
    void write(MonitorEventEntry entry) throws IOException;

    /**
     * Writes the event data to a destination.
     *
     * @param level     the monitor level to write
     * @param timestamp the timestamp
     * @param source    the event source
     * @param args      the arguments
     * @throws IOException if there is a dispatch error
     */
    void write(MonitorLevel level, long timestamp, String source, String template, Object[] args) throws IOException;
}

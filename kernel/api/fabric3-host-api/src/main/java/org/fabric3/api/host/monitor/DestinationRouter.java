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
package org.fabric3.api.host.monitor;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * Routes monitor events to a destination.
 */
public interface DestinationRouter {

    /**
     * The default router destination.
     */
    String DEFAULT_DESTINATION = "default";

    /**
     * The default destination index.
     */
    int DEFAULT_DESTINATION_INDEX = 0;

    /**
     * Returns the index to use for dispatching a message to a destination.
     *
     * @param name the destination name
     * @return the destination index
     */
    int getDestinationIndex(String name);

    /**
     * Sends the monitor event to a destination.
     *
     * @param level            the level
     * @param destinationIndex the destination index
     * @param timestamp        the event timestamp
     * @param source           the source emitting the event
     * @param message          the event message
     * @param parse            true if the message should be parsed as a template
     * @param args             event arguments
     */
    void send(MonitorLevel level, int destinationIndex, long timestamp, String source, String message, boolean parse, Object... args);

}

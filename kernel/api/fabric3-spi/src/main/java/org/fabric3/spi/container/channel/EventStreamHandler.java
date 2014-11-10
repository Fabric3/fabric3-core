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
package org.fabric3.spi.container.channel;

/**
 * Processes events flowing through an event stream.
 */
public interface EventStreamHandler {

    /**
     * Process the event.
     *
     * @param event      the event.
     * @param endOfBatch flag to indicate if this is the last event in a batch from the channel
     */
    void handle(Object event, boolean endOfBatch);

    /**
     * Sets the next handler in the handler chain.
     *
     * @param next the next EventStreamHandler
     */
    void setNext(EventStreamHandler next);

    /**
     * Returns the next handler in the handler chain.
     *
     * @return the next EventStreamHandler
     */
    EventStreamHandler getNext();

}

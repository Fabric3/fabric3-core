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

import org.fabric3.spi.model.physical.PhysicalEventStream;

/**
 * Transmits events to a channel. As events are transmitted through the stream, they are processed by a series of handlers.
 */
public interface EventStream {

    /**
     * Returns the physical event stream metadata.
     *
     * @return the physical event stream metadata
     */
    PhysicalEventStream getDefinition();

    /**
     * Returns the head handler for the stream.
     *
     * @return the head handler for the stream
     */
    EventStreamHandler getHeadHandler();

    /**
     * Returns the tail handler for the stream.
     *
     * @return the tail handler for the stream
     */
    EventStreamHandler getTailHandler();

    /**
     * Adds a handler to the stream.
     *
     * @param handler the handler to add
     */
    void addHandler(EventStreamHandler handler);

    /**
     * Adds a handler to the stream at the given index.
     *
     * @param index   the location where to add the handler
     * @param handler the handler to add
     */
    void addHandler(int index, EventStreamHandler handler);

}

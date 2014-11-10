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
package org.fabric3.channel.impl;

import java.net.URI;

import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Broadcasts a received event to a collection of event stream handlers.
 */
public interface FanOutHandler extends EventStreamHandler {

    /**
     * Adds a connection containing the event streams.
     *
     * @param uri        the connection uri
     * @param connection the connection
     */
    void addConnection(URI uri, ChannelConnection connection);

    /**
     * Removes a connection
     *
     * @param uri the connection uri
     * @return the removed connection
     */
    ChannelConnection removeConnection(URI uri);

}
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Base FanOutHandler functionality.
 *
 * Supports registering a connection multiple times. This is required for producer-side channels where a connection from the channel to a binding may be
 * provisioned multiple times if there is more than one producer connected to the same channel provisioned in a zone.
 */
public abstract class AbstractFanOutHandler implements FanOutHandler {
    protected Map<URI, ChannelConnection> connectionMap = new HashMap<>();
    protected Map<URI, AtomicInteger> counterMap = new HashMap<>();

    protected ChannelConnection[] connections = new ChannelConnection[0];

    public synchronized void addConnection(URI uri, ChannelConnection connection) {
        AtomicInteger count = counterMap.get(uri);
        if (count == null) {
            count = new AtomicInteger(1);
            counterMap.put(uri, count);
            connectionMap.put(uri, connection);
            connections = connectionMap.values().toArray(new ChannelConnection[connectionMap.size()]);
        } else {
            count.incrementAndGet();
        }
    }

    public synchronized ChannelConnection removeConnection(URI uri) {
        int count = counterMap.get(uri).decrementAndGet();
        if (count == 0) {
            counterMap.remove(uri);
            ChannelConnection connection = connectionMap.remove(uri);
            connections = connectionMap.values().toArray(new ChannelConnection[connectionMap.size()]);
            return connection;
        } else {
            return connectionMap.get(uri);
        }
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

}
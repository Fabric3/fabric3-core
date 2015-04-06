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
package org.fabric3.channel.disruptor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.lmax.disruptor.EventHandler;
import org.fabric3.api.ChannelEvent;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 *
 */
public class EventHandlerHelper {

    /**
     * Creates event handlers from a collection of {@link ChannelConnection}s and returns a Map sorted by connection sequence value.
     *
     * @param connections the connections
     * @return the sorted Map
     */
    public static NavigableMap<Integer, List<EventHandler<RingBufferEvent>>> createAndSort(Collection<ChannelConnection> connections) {
        NavigableMap<Integer, List<EventHandler<RingBufferEvent>>> sorted = new TreeMap<>();

        for (ChannelConnection connection : connections) {
            Integer sequence = connection.getSequence();
            List<EventHandler<RingBufferEvent>> handlers = sorted.get(sequence);
            if (handlers == null) {
                handlers = new ArrayList<>();
                sorted.put(sequence, handlers);
            }
            boolean channelEvent = isChannelEvent(connection);
            handlers.add(new ChannelEventHandler(connection, channelEvent));
        }
        return sorted;
    }

    public static boolean isChannelEvent(ChannelConnection connection) {
        return ChannelEvent.class.isAssignableFrom(connection.getEventStream().getEventType());
    }

    private EventHandlerHelper() {
    }
}

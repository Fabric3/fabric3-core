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

import com.lmax.disruptor.EventHandler;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;

/**
 * Dispatches an event from the channel ring buffer to consumer streams.
 */
public class ChannelEventHandler implements EventHandler<RingBufferEvent> {
    private EventStream stream;

    public ChannelEventHandler(ChannelConnection connection) {
        stream = connection.getEventStream();
    }

    public void onEvent(RingBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (stream.getDefinition().isChannelEvent()) {
            // consumer takes a channel event, send that, making sure to set the end-of-batch marker and sequence number
            event.setEndOfBatch(endOfBatch);
            event.setSequence(sequence);
            stream.getHeadHandler().handle(event, endOfBatch);
        } else {
            // if the parsed value has been set, send that
            Object parsed = event.getParsed(Object.class);
            if (parsed != null) {
                stream.getHeadHandler().handle(parsed, endOfBatch);
            } else {
                stream.getHeadHandler().handle(event.getEvent(Object.class), endOfBatch);
            }
        }
    }
}

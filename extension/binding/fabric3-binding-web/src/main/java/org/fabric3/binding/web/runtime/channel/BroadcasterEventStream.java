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
package org.fabric3.binding.web.runtime.channel;

import org.atmosphere.cpr.Broadcaster;

import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * Receives events flowing through a channel and broadcasts them to subscribed websocket and comet connections.
 */
public class BroadcasterEventStream implements EventStream, EventStreamHandler {
    private Broadcaster broadcaster;

    public BroadcasterEventStream(Broadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    public void handle(Object event, boolean endOfBatch) {
        broadcaster.broadcast(event);
    }

    public PhysicalEventStreamDefinition getDefinition() {
        return null;
    }

    public EventStreamHandler getHeadHandler() {
        return this;
    }

    public EventStreamHandler getTailHandler() {
        return this;
    }

    public void addHandler(EventStreamHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void addHandler(int index, EventStreamHandler handler) {
        throw new UnsupportedOperationException();
    }

    public void setNext(EventStreamHandler next) {
        throw new UnsupportedOperationException();
    }

    public EventStreamHandler getNext() {
        return null;
    }
}
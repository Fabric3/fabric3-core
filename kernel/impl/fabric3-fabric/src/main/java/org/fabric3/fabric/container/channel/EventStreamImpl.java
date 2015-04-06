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
package org.fabric3.fabric.container.channel;

import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.util.Closeable;

/**
 *
 */
public class EventStreamImpl implements EventStream {
    private Class<?> eventType;
    private EventStreamHandler headHandler;
    private EventStreamHandler tailHandler;
    private Closeable closeable;

    public EventStreamImpl(Class<?> eventType) {
        this.eventType = eventType;
        PassThroughHandler handler = new PassThroughHandler();
        addHandler(handler);
    }

    public Class<?> getEventType() {
        return eventType;
    }

    public EventStreamHandler getHeadHandler() {
        return headHandler;
    }

    public EventStreamHandler getTailHandler() {
        return tailHandler;
    }

    public void addHandler(EventStreamHandler handler) {
        if (headHandler == null) {
            headHandler = handler;
        } else {
            tailHandler.setNext(handler);
        }
        tailHandler = handler;
    }

    public void addHandler(int index, EventStreamHandler handler) {
        int i = 0;
        EventStreamHandler next = headHandler;
        EventStreamHandler prev = null;
        while (next != null && i < index) {
            prev = next;
            next = next.getNext();
            i++;
        }
        if (i == index) {
            if (prev != null) {
                prev.setNext(handler);
            } else {
                headHandler = handler;
            }
            handler.setNext(next);
            if (next == null) {
                tailHandler = handler;
            }
        } else {
            throw new ArrayIndexOutOfBoundsException(index);
        }
    }

    public void close() {
        if (closeable != null) {
            closeable.close();
        }
    }

    public void setCloseable(Closeable closeable) {
        this.closeable = closeable;
    }
}
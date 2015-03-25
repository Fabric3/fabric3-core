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

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 * The default Channel implementation.
 */
public class DefaultChannelImpl implements Channel {
    private URI uri;
    private QName deployable;
    private final ChannelSide channelSide;

    private EventStreamHandler headHandler;
    private EventStreamHandler tailHandler;
    private EventStreamHandler inHandler;
    private FanOutHandler fanOutHandler;

    public DefaultChannelImpl(URI uri, QName deployable, FanOutHandler fanOutHandler, ChannelSide channelSide) {
        this.uri = uri;
        this.deployable = deployable;
        this.channelSide = channelSide;
        inHandler = new PassThroughHandler();
        this.fanOutHandler = fanOutHandler;
        inHandler.setNext(this.fanOutHandler);
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

    public void addHandler(EventStreamHandler handler) {
        if (headHandler == null) {
            headHandler = handler;
            inHandler.setNext(handler);
        } else {
            tailHandler.setNext(handler);
        }
        tailHandler = handler;
        tailHandler.setNext(fanOutHandler);
    }

    public void removeHandler(EventStreamHandler handler) {
        EventStreamHandler current = headHandler;
        EventStreamHandler previous = null;
        while (current != null) {
            if (current == handler) {
                if (headHandler == current) {
                    headHandler = current.getNext();
                }
                if (tailHandler == current) {
                    tailHandler = previous == null ? headHandler : previous;
                }
                if (previous != null) {
                    previous.setNext(current.getNext());
                }
                inHandler.setNext(headHandler);
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    public void attach(EventStreamHandler handler) {
        handler.setNext(inHandler);
    }

    public void attach(ChannelConnection connection) {
        EventStream stream = connection.getEventStream();
        stream.getTailHandler().setNext(inHandler);
    }

    public void subscribe(URI uri, ChannelConnection connection) {
        fanOutHandler.addConnection(uri, connection);
    }

    public ChannelConnection unsubscribe(URI uri) {
        return fanOutHandler.removeConnection(uri);
    }

    public ChannelSide getChannelSide() {
        return channelSide;
    }

    public Object getDirectConnection() {
        return headHandler;
    }
}
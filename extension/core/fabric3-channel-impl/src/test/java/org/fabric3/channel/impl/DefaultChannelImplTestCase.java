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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 * The default Channel implementation.
 */
public class DefaultChannelImplTestCase extends TestCase {
    private DefaultChannelImpl channel;
    private FanOutHandler fanOutHandler;

    public void testAddRemoveHandler() throws Exception {
        fanOutHandler.handle(EasyMock.notNull(), EasyMock.anyBoolean());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(fanOutHandler);

        BlockingHandler handler = new BlockingHandler();
        BlockingHandler handler2 = new BlockingHandler();
        channel.addHandler(handler);
        channel.addHandler(handler2);

        EventStreamHandler head = new PassThroughHandler();
        channel.attach(head);

        channel.removeHandler(handler);
        handler.setClosed(true);

        head.handle(new Object(), true);

        channel.removeHandler(handler2);
        handler2.setClosed(true);

        head.handle(new Object(), true);
        EasyMock.verify(fanOutHandler);
    }

    public void testSubscribeUnsubscribe() throws Exception {

        URI uri = URI.create("connection");
        ChannelConnection connection = EasyMock.createNiceMock(ChannelConnection.class);

        fanOutHandler.addConnection(uri, connection);
        EasyMock.expect(fanOutHandler.removeConnection(uri)).andReturn(connection);
        EasyMock.replay(connection, fanOutHandler);

        channel.subscribe(uri, connection);
        assertEquals(connection, channel.unsubscribe(uri));

        EasyMock.verify(connection, fanOutHandler);

    }

    public void testAttachConnection() throws Exception {
        EventStreamHandler handler = EasyMock.createMock(EventStreamHandler.class);
        handler.setNext(EasyMock.isA(EventStreamHandler.class));
        EventStream stream = EasyMock.createMock(EventStream.class);
        EasyMock.expect(stream.getTailHandler()).andReturn(handler);
        ChannelConnection connection = EasyMock.createNiceMock(ChannelConnection.class);
        EasyMock.expect(connection.getEventStream()).andReturn(stream);

        EasyMock.replay(handler, stream, connection);

        channel.attach(connection);
        EasyMock.verify(handler, stream, connection);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fanOutHandler = EasyMock.createMock(FanOutHandler.class);
        channel = new DefaultChannelImpl(URI.create("channel"), fanOutHandler, ChannelSide.CONSUMER, URI.create("test"));
    }

    private class BlockingHandler extends PassThroughHandler {
        private boolean closed;

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public void handle(Object event, boolean endOfBatch) {
            if (closed) {
                fail("Handler not properly removed");
            }
            super.handle(event, true);
        }
    }
}
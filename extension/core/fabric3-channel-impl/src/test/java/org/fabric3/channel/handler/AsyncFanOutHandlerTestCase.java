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
package org.fabric3.channel.handler;

import java.net.URI;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.channel.impl.AsyncFanOutHandler;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 *
 */
public class AsyncFanOutHandlerTestCase extends TestCase {

    public void testFanOut() throws Exception {

        ExecutorService executorService = createExecutor();

        AsyncFanOutHandler handler = new AsyncFanOutHandler(executorService);

        Object event = new Object();

        EventStreamHandler handler1 = EasyMock.createMock(EventStreamHandler.class);
        ChannelConnection connection1 = createConnection(handler1);

        EventStreamHandler handler2 = EasyMock.createMock(EventStreamHandler.class);
        ChannelConnection connection2 = createConnection(handler2);

        handler.addConnection(URI.create("connection1"), connection1);
        handler.addConnection(URI.create("connection2"), connection2);

        handler1.handle(event, true);
        handler2.handle(event, true);

        EasyMock.replay(executorService, handler1, handler2);

        handler.handle(event, true);

        EasyMock.verify(executorService, handler1, handler2);
    }

    private ExecutorService createExecutor() {
        ExecutorService executorService = EasyMock.createMock(ExecutorService.class);
        executorService.execute(EasyMock.isA(Runnable.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Runnable runnable = (Runnable) EasyMock.getCurrentArguments()[0];
                runnable.run();
                return null;
            }
        });
        return executorService;
    }

    private ChannelConnection createConnection(EventStreamHandler handler) {
        EventStream stream = EasyMock.createMock(EventStream.class);
        EasyMock.expect(stream.getHeadHandler()).andReturn(handler);

        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(connection.getEventStream()).andReturn(stream);
        EasyMock.replay(stream, connection);
        return connection;
    }

}
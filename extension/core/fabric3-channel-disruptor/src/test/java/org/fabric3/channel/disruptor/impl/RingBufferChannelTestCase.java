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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.lmax.disruptor.BlockingWaitStrategy;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 *
 */
public class RingBufferChannelTestCase extends TestCase {

    public void testDispatch() throws Exception {
        RingBufferChannel channel = new RingBufferChannel(URI.create("channel"),
                                                          new QName("test", "test"),
                                                          1024,
                                                          new BlockingWaitStrategy(),
                                                          ChannelSide.CONSUMER,
                                                          Executors.newScheduledThreadPool(4));

        MockConsumer consumer = new MockConsumer();

        EventStream stream = EasyMock.createMock(EventStream.class);
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition("test");
        EasyMock.expect(stream.getDefinition()).andReturn(definition).atLeastOnce();
        EasyMock.expect(stream.getHeadHandler()).andReturn(consumer).atLeastOnce();

        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(connection.getSequence()).andReturn(0);
        EasyMock.expect(connection.getEventStream()).andReturn(stream);

        EasyMock.replay(connection, stream);

        PassThroughHandler producer = new PassThroughHandler();

        channel.subscribe(URI.create("test"), connection);
        channel.attach(producer);

        channel.start();

        producer.handle(new Object(), true);
        producer.handle(new Object(), true);

        consumer.latch.await();

        EasyMock.verify(connection, stream);
    }

    private class MockConsumer implements EventStreamHandler {
        private CountDownLatch latch = new CountDownLatch(2);

        public void handle(Object event, boolean endOfBatch) {
            latch.countDown();
        }

        public void setNext(EventStreamHandler next) {
        }

        public EventStreamHandler getNext() {
            return null;
        }
    }
}

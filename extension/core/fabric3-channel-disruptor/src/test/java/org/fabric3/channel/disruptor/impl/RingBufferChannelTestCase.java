/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.channel.disruptor.impl;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import com.lmax.disruptor.BlockingWaitStrategy;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.PassThroughHandler;

/**
 *
 */
public class RingBufferChannelTestCase extends TestCase {

    public void testDispatch() throws Exception {
        RingBufferChannel channel = new RingBufferChannel(URI.create("channel"),
                                                          new QName("test", "test"),
                                                          1024,
                                                          new BlockingWaitStrategy(),
                                                          Executors.newScheduledThreadPool(4));

        MockConsumer consumer = new MockConsumer();

        EventStream stream = EasyMock.createMock(EventStream.class);
        EasyMock.expect(stream.getHeadHandler()).andReturn(consumer).atLeastOnce();

        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);
        List<EventStream> eventStreams = Collections.singletonList(stream);
        EasyMock.expect(connection.getEventStreams()).andReturn(eventStreams);

        EasyMock.replay(connection, stream);

        PassThroughHandler producer = new PassThroughHandler();

        channel.subscribe(URI.create("test"), connection);
        channel.attach(producer);

        channel.start();

        producer.handle(new Object());
        producer.handle(new Object());

        consumer.latch.await();

        EasyMock.verify(connection, stream);
    }

    private class MockConsumer implements EventStreamHandler {
        private CountDownLatch latch = new CountDownLatch(2);

        public void handle(Object event) {
            latch.countDown();
        }

        public void setNext(EventStreamHandler next) {
        }

        public EventStreamHandler getNext() {
            return null;
        }
    }
}

/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.handler.AsyncFanOutHandler;
import org.fabric3.spi.host.Port;

/**
 * @version $Revision: 10396 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class NonReliableSubscriberTestCase extends TestCase {

    private static final SocketAddress ADDRESS = new SocketAddress("runtime", "tcp", "10.10.10.1", new Port(){
        public String getName() {
            return null;
        }

        public int getNumber() {
            return 1061;
        }

        public void releaseLock() {

        }
    });

    private static final SocketAddress ADDRESS2 = new SocketAddress("runtime", "tcp", "10.10.10.2", new Port(){
        public String getName() {
            return null;
        }

        public int getNumber() {
            return 1061;
        }

        public void releaseLock() {

        }
    });
    private MessagingMonitor monitor;

    public void testReceive() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        ZMQ.Socket socket = EasyMock.createMock(ZMQ.Socket.class);
        socket.subscribe(EasyMock.isA(byte[].class));
        socket.connect(EasyMock.eq(ADDRESS.toProtocolString()));
        EasyMock.expect(socket.recv(0)).andStubAnswer(new IAnswer<byte[]>() {

            public byte[] answer() throws Throwable {
                return "test".getBytes();
            }
        });
        socket.close();

        AsyncFanOutHandler head = EasyMock.createMock(AsyncFanOutHandler.class);
        head.handle(EasyMock.isA(Object.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                latch.countDown();
                return null;
            }
        });

        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        EasyMock.expect(context.socket(ZMQ.SUB)).andReturn(socket);

        ZMQ.Poller poller = EasyMock.createMock(ZMQ.Poller.class);
        EasyMock.expect(poller.poll()).andReturn(1l);

        EasyMock.expect(poller.register(socket)).andReturn(1);

        EasyMock.expect(context.poller()).andReturn(poller);


        MessagingMonitor monitor = EasyMock.createMock(MessagingMonitor.class);

        EasyMock.replay(monitor);
        EasyMock.replay(poller);
        EasyMock.replay(context);
        EasyMock.replay(socket);
        EasyMock.replay(head);

        List<SocketAddress> addresses = Collections.singletonList(ADDRESS);
        NonReliableSubscriber subscriber = new NonReliableSubscriber("", context, addresses, head, monitor);
        subscriber.start();

        latch.await(10000, TimeUnit.MILLISECONDS);

        subscriber.stop();

        EasyMock.verify(monitor);
        EasyMock.verify(poller);
        EasyMock.verify(context);
        EasyMock.verify(socket);
        EasyMock.verify(head);
    }


    public void testChangeAddress() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(2);
        final AtomicBoolean pastLatch1 = new AtomicBoolean();
        ZMQ.Socket socket = createSocket(ADDRESS);
        ZMQ.Socket socket2 = createSocket(ADDRESS2);

        AsyncFanOutHandler head = EasyMock.createMock(AsyncFanOutHandler.class);
        head.handle(EasyMock.isA(Object.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                if (!pastLatch1.get()) {
                    latch.countDown();
                } else {
                    latch2.countDown();
                }
                return null;
            }
        });


        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        EasyMock.expect(context.socket(ZMQ.SUB)).andReturn(socket);
        EasyMock.expect(context.socket(ZMQ.SUB)).andReturn(socket2);

        ZMQ.Poller poller = EasyMock.createMock(ZMQ.Poller.class);
        EasyMock.expect(poller.poll()).andReturn(1l).atLeastOnce();
        EasyMock.expect(poller.register(socket)).andReturn(1);

        ZMQ.Poller poller2 = EasyMock.createMock(ZMQ.Poller.class);
        EasyMock.expect(poller2.poll()).andReturn(1l).atLeastOnce();
        EasyMock.expect(poller2.register(socket2)).andReturn(1);

        EasyMock.expect(context.poller()).andReturn(poller);
        EasyMock.expect(context.poller()).andReturn(poller2);


        EasyMock.replay(monitor);
        EasyMock.replay(poller);
        EasyMock.replay(poller2);
        EasyMock.replay(context);
        EasyMock.replay(socket);
        EasyMock.replay(socket2);
        EasyMock.replay(head);

        List<SocketAddress> addresses = Collections.singletonList(ADDRESS);
        NonReliableSubscriber subscriber = new NonReliableSubscriber("", context, addresses, head, monitor);
        subscriber.start();

        latch.await();
        subscriber.onUpdate(Collections.singletonList(ADDRESS2));
        pastLatch1.set(true);

        latch2.await();

        subscriber.stop();

        EasyMock.verify(monitor);
        EasyMock.verify(poller);
        EasyMock.verify(poller2);
        EasyMock.verify(context);
        EasyMock.verify(socket);
        EasyMock.verify(socket2);
        EasyMock.verify(head);
    }


    public void testRescheduleAfterUncheckedException() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        ZMQ.Socket socket = createSocket(ADDRESS);

        AsyncFanOutHandler head = EasyMock.createStrictMock(AsyncFanOutHandler.class);
        head.handle(EasyMock.isA(Object.class));
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {
                latch.countDown();
                return null;
            }
        });


        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        EasyMock.expect(context.socket(ZMQ.SUB)).andReturn(socket);

        ZMQ.Poller poller = EasyMock.createMock(ZMQ.Poller.class);
        EasyMock.expect(poller.poll()).andReturn(1l).atLeastOnce();

        EasyMock.expect(poller.register(socket)).andReturn(1);

        EasyMock.expect(context.poller()).andReturn(poller);


        MessagingMonitor monitor = EasyMock.createMock(MessagingMonitor.class);

        EasyMock.replay(monitor);
        EasyMock.replay(poller);
        EasyMock.replay(context);
        EasyMock.replay(socket);
        EasyMock.replay(head);

        List<SocketAddress> addresses = Collections.singletonList(ADDRESS);
        NonReliableSubscriber subscriber = new NonReliableSubscriber("", context, addresses, head, monitor);
        subscriber.start();

        latch.await();
        subscriber.stop();

        EasyMock.verify(monitor);
        EasyMock.verify(poller);
        EasyMock.verify(context);
        EasyMock.verify(socket);
        EasyMock.verify(head);
    }

    private ZMQ.Socket createSocket(SocketAddress address) {
        ZMQ.Socket socket = EasyMock.createMock(ZMQ.Socket.class);
        socket.subscribe(EasyMock.isA(byte[].class));
        socket.connect(EasyMock.eq(address.toProtocolString()));
        EasyMock.expect(socket.recv(0)).andStubAnswer(new IAnswer<byte[]>() {

            public byte[] answer() throws Throwable {
                return "test".getBytes();
            }
        });
        socket.close();
        return socket;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        monitor = EasyMock.createMock(MessagingMonitor.class);

    }
}

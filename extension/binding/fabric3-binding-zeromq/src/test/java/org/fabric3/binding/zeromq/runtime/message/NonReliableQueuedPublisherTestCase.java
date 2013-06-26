/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.easymock.IAnswer;
import org.easymock.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.JDK7WorkaroundHelper;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.host.Port;

/**
 *
 */
public class NonReliableQueuedPublisherTestCase extends TestCase {
    private static final SocketAddress ADDRESS = new SocketAddress("runtime", "tcp", "10.10.10.1", new Port() {
        public String getName() {
            return null;
        }

        public int getNumber() {
            return 1061;
        }

        public void bind(TYPE type) {

        }

        public void release() {

        }
    });

    public void testPublish() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        byte[] message = "test".getBytes();

        ZMQ.Socket socket = EasyMock.createMock(ZMQ.Socket.class);
        socket.setLinger(0);
        socket.setHWM(1000);
        socket.bind(ADDRESS.toProtocolString());
        EasyMock.expect(socket.send(message, 0)).andStubAnswer(new IAnswer<Boolean>() {

            public Boolean answer() throws Throwable {
                latch.countDown();
                return true;
            }
        });

        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        EasyMock.expect(context.socket(ZMQ.PUB)).andReturn(socket);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expect(manager.getContext()).andReturn(context);
        MessagingMonitor monitor = EasyMock.createMock(MessagingMonitor.class);

        EasyMock.replay(monitor);
        EasyMock.replay(manager);
        EasyMock.replay(context);
        EasyMock.replay(socket);

        ZeroMQMetadata metadata = new ZeroMQMetadata();

        NonReliableQueuedPublisher publisher = new NonReliableQueuedPublisher(manager, ADDRESS, metadata, 1000, monitor);
        publisher.start();
        publisher.publish(message);

        latch.await(10000, TimeUnit.MILLISECONDS);

        EasyMock.verify(monitor);
        EasyMock.verify(manager);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
    }
}

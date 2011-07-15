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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.host.Port;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class RoundRobinSocketMultiplexerTestCase extends TestCase {
    private ZeroMQMetadata metadata = new ZeroMQMetadata();

    public void testRoundRobin() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(context, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.replay(context);

        SocketAddress address1 = createAddress(1);
        SocketAddress address2 = createAddress(2);
        SocketAddress address3 = createAddress(3);

        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address1);
        list.add(address2);
        list.add(address3);

        multiplexer.update(list);

        List<ZMQ.Socket> order = new ArrayList<ZMQ.Socket>();
        ZMQ.Socket next = multiplexer.get();
        order.add(next);
        next = multiplexer.get();
        assertFalse(order.contains(next));
        order.add(next);
        next = multiplexer.get();
        assertFalse(order.contains(next));
        order.add(next);

        for (ZMQ.Socket socket : order) {
            assertSame(socket, multiplexer.get());
        }

        for (ZMQ.Socket socket : order) {
            assertSame(socket, multiplexer.get());
        }
    }

    public void testSingletonIterator() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(context, ZMQ.PULL, metadata);
        ZMQ.Socket socket = EasyMock.createMock(ZMQ.Socket.class);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket);
        EasyMock.replay(context);

        SocketAddress address = createAddress(1);
        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address);

        multiplexer.update(list);

        assertSame(socket, multiplexer.get());
        assertSame(socket, multiplexer.get());
    }

    public void testUpdateAdd() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(context, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        socket1.connect("tcp://1:1");
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        socket2.connect("tcp://2:2");
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        socket3.connect("tcp://3:3");
        ZMQ.Socket socket4 = EasyMock.createMock(ZMQ.Socket.class);
        socket4.connect("tcp://4:4");
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket4);
        EasyMock.replay(context);
        EasyMock.replay(socket1);
        EasyMock.replay(socket2);
        EasyMock.replay(socket3);
        EasyMock.replay(socket4);

        SocketAddress address1 = createAddress(1);
        SocketAddress address2 = createAddress(2);
        SocketAddress address3 = createAddress(3);
        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address1);
        list.add(address2);
        list.add(address3);

        multiplexer.update(list);
        multiplexer.get();

        SocketAddress address4 = createAddress(4);
        list.add(address4);
        multiplexer.update(list);

        List<ZMQ.Socket> order = new ArrayList<ZMQ.Socket>();
        ZMQ.Socket next = multiplexer.get();
        order.add(next);
        next = multiplexer.get();
        assertFalse(order.contains(next));
        order.add(next);
        next = multiplexer.get();
        assertFalse(order.contains(next));
        order.add(next);
        next = multiplexer.get();
        assertFalse(order.contains(next));
        order.add(next);

        for (ZMQ.Socket socket : order) {
            assertSame(socket, multiplexer.get());
        }

        for (ZMQ.Socket socket : order) {
            assertSame(socket, multiplexer.get());
        }

        EasyMock.verify(context);
        EasyMock.verify(socket1);
        EasyMock.verify(socket2);
        EasyMock.verify(socket3);
        EasyMock.verify(socket4);
    }

    public void testUpdateRemove() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(context, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        socket1.connect("tcp://1:1");
        socket1.close();
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        socket2.connect("tcp://2:2");
        socket2.close();
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        socket3.connect("tcp://3:3");
        socket3.close();
        ZMQ.Socket socket4 = EasyMock.createMock(ZMQ.Socket.class);
        socket4.connect("tcp://4:4");
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket4);
        EasyMock.replay(context);
        EasyMock.replay(socket1);
        EasyMock.replay(socket2);
        EasyMock.replay(socket3);
        EasyMock.replay(socket4);

        SocketAddress address1 = createAddress(1);
        SocketAddress address2 = createAddress(2);
        SocketAddress address3 = createAddress(3);
        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address1);
        list.add(address2);
        list.add(address3);

        multiplexer.update(list);
        multiplexer.get();

        SocketAddress address4 = createAddress(4);
        multiplexer.update(Collections.singletonList(address4));

        assertSame(socket4, multiplexer.get());
        assertSame(socket4, multiplexer.get());
        assertSame(socket4, multiplexer.get());
        assertSame(socket4, multiplexer.get());


        EasyMock.verify(context);
        EasyMock.verify(socket1);
        EasyMock.verify(socket2);
        EasyMock.verify(socket3);
        EasyMock.verify(socket4);

    }

    private SocketAddress createAddress(final int port) {
        return new SocketAddress("vm", "tcp", String.valueOf(port), new Port() {
            public String getName() {
                return null;
            }

            public int getNumber() {
                return port;
            }

            public void releaseLock() {

            }
        });
    }


}

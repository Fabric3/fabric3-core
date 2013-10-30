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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.zeromq.ZMQ;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.JDK7WorkaroundHelper;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.host.Port;

/**
 *
 */
public class RoundRobinSocketMultiplexerTestCase extends TestCase {
    private ZeroMQMetadata metadata = new ZeroMQMetadata();

    public void testRoundRobin() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(3);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(3);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.replay(context);
        EasyMock.replay(manager);

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
        multiplexer.close();
        EasyMock.verify(manager);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);

    }

    public void testSingletonIterator() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);

        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expect(manager.getContext()).andReturn(context);
        manager.release(EasyMock.isA(String.class));

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
        ZMQ.Socket socket = EasyMock.createMock(ZMQ.Socket.class);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket);
        EasyMock.replay(context);
        EasyMock.replay(manager);

        SocketAddress address = createAddress(1);
        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address);

        multiplexer.update(list);

        assertSame(socket, multiplexer.get());
        assertSame(socket, multiplexer.get());

        multiplexer.close();

        EasyMock.verify(manager);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
    }

    public void testUpdateAdd() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        socket1.setLinger(0);
        socket1.setHWM(1000);
        socket1.connect("tcp://1:1");
        socket1.close();
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        socket2.setLinger(0);
        socket2.setHWM(1000);
        socket2.connect("tcp://2:2");
        socket2.close();
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        socket3.setLinger(0);
        socket3.setHWM(1000);
        socket3.connect("tcp://3:3");
        socket3.close();
        ZMQ.Socket socket4 = EasyMock.createMock(ZMQ.Socket.class);
        socket4.setLinger(0);
        socket4.setHWM(1000);
        socket4.connect("tcp://4:4");
        socket4.close();
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket4);
        EasyMock.replay(context);
        EasyMock.replay(manager);
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

        multiplexer.close();

        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        EasyMock.verify(manager);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket1, socket2, socket3, socket4);
    }

    public void testUpdateRemove() throws Exception {
        ZMQ.Context context = EasyMock.createMock(ZMQ.Context.class);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);

        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
        ZMQ.Socket socket1 = EasyMock.createMock(ZMQ.Socket.class);
        socket1.setLinger(0);
        socket1.setHWM(1000);
        socket1.connect("tcp://1:1");
        socket1.close();
        ZMQ.Socket socket2 = EasyMock.createMock(ZMQ.Socket.class);
        socket2.setLinger(0);
        socket2.setHWM(1000);
        socket2.connect("tcp://2:2");
        socket2.close();
        ZMQ.Socket socket3 = EasyMock.createMock(ZMQ.Socket.class);
        socket3.setLinger(0);
        socket3.setHWM(1000);
        socket3.connect("tcp://3:3");
        socket3.close();
        ZMQ.Socket socket4 = EasyMock.createMock(ZMQ.Socket.class);
        socket4.setLinger(0);
        socket4.connect("tcp://4:4");
        socket4.setHWM(1000);
        socket4.close();
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket1);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket2);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket3);
        EasyMock.expect(context.socket(ZMQ.PULL)).andReturn(socket4);
        EasyMock.replay(context);
        EasyMock.replay(socket1);
        EasyMock.replay(socket2);
        EasyMock.replay(socket3);
        EasyMock.replay(socket4);
        EasyMock.replay(manager);

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

        multiplexer.close();

        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        EasyMock.verify(manager);
        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(socket1, socket2, socket3, socket4);

    }

    private SocketAddress createAddress(final int port) {
        return new SocketAddress("vm", "zone", "tcp", String.valueOf(port), new Port() {
            public String getName() {
                return null;
            }

            public int getNumber() {
                return port;
            }

            public void bind(TYPE type) {

            }

            public void release() {

            }
        });
    }

}

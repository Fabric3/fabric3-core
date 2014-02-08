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
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.zeromq.ZMQ;

/**
 *
 */
public class RoundRobinSocketMultiplexerTestCase extends TestCase {
    private ZeroMQMetadata metadata = new ZeroMQMetadata();

    public void testRoundRobin() throws Exception {
        ZMQ.Context context = ZMQ.context(1);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(3);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(3);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
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

    }

    public void testSingletonIterator() throws Exception {
        ZMQ.Context context = ZMQ.context(1);

        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expect(manager.getContext()).andReturn(context);
        manager.release(EasyMock.isA(String.class));

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);
        EasyMock.replay(manager);

        SocketAddress address = createAddress(1);
        List<SocketAddress> list = new ArrayList<SocketAddress>();
        list.add(address);

        multiplexer.update(list);

        assertSame(multiplexer.get(), multiplexer.get());

        multiplexer.close();

        EasyMock.verify(manager);
    }

    public void testUpdateAdd() throws Exception {
        ZMQ.Context context = ZMQ.context(1);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);

        EasyMock.replay(manager);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);

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

        EasyMock.verify(manager);
    }

    public void testUpdateRemove() throws Exception {
        ZMQ.Context context = ZMQ.context(1);
        ContextManager manager = EasyMock.createMock(ContextManager.class);
        EasyMock.expect(manager.getContext()).andReturn(context).atLeastOnce();
        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);

        manager.release(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(4);
        EasyMock.replay(manager);

        RoundRobinSocketMultiplexer multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PULL, metadata);


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
        ZMQ.Socket socket =  multiplexer.get();
        assertSame(socket, multiplexer.get());
        assertSame(socket, multiplexer.get());
        assertSame(socket, multiplexer.get());

        multiplexer.close();

        EasyMock.verify(manager);
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

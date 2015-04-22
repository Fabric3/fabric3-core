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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
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

        List<SocketAddress> list = new ArrayList<>();
        list.add(address1);
        list.add(address2);
        list.add(address3);

        multiplexer.update(list);

        List<ZMQ.Socket> order = new ArrayList<>();
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
        List<SocketAddress> list = new ArrayList<>();
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
        List<SocketAddress> list = new ArrayList<>();
        list.add(address1);
        list.add(address2);
        list.add(address3);

        multiplexer.update(list);
        multiplexer.get();

        SocketAddress address4 = createAddress(4);
        list.add(address4);
        multiplexer.update(list);

        List<ZMQ.Socket> order = new ArrayList<>();
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
        List<SocketAddress> list = new ArrayList<>();
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
        return new SocketAddress("tcp", String.valueOf(port), new Port() {
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

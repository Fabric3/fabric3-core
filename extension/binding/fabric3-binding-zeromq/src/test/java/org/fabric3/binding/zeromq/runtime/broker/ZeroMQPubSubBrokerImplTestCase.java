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
package org.fabric3.binding.zeromq.runtime.broker;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.JDK7WorkaroundHelper;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.TransformerHandlerFactory;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.zeromq.ZMQ;

/**
 *
 */
public class ZeroMQPubSubBrokerImplTestCase extends TestCase {
    private static final SocketAddress ADDRESS = new SocketAddress("runtime", "zone", "tcp", "10.10.10.1", new Port() {
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

    private ContextManager manager;
    private AddressCache addressCache;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private ZMQ.Context context;
    private ChannelConnection connection;
    private ZeroMQPubSubBrokerImpl broker;
    private PortAllocator allocator;
    private HostInfo info;
    private ZeroMQManagementService managementService;
    private TransformerHandlerFactory handlerFactory;

    public void testSubscribeUnsubscribe() throws Exception {

        EasyMock.expect(addressCache.getActiveAddresses("endpoint")).andReturn(Collections.singletonList(ADDRESS));
        addressCache.subscribe(EasyMock.eq("endpoint"), EasyMock.isA(Subscriber.class));
        EasyMock.expectLastCall();

        EventStream stream = EasyMock.createMock(EventStream.class);
        PhysicalEventStreamDefinition definition = EasyMock.createMock(PhysicalEventStreamDefinition.class);
        EasyMock.expect(definition.getEventTypes()).andReturn(Collections.<String>emptyList());
        EasyMock.expect(stream.getDefinition()).andReturn(definition);
        EventStreamHandler headHandler = EasyMock.createMock(EventStreamHandler.class);

        EasyMock.expect(stream.getHeadHandler()).andReturn(headHandler);

        EasyMock.expect(connection.getEventStream()).andReturn(stream).atLeastOnce();

        EasyMock.expect(handlerFactory.createHandler(EasyMock.isA(DataType.class), EasyMock.isA(DataType.class), EasyMock.isA(ClassLoader.class))).andReturn(
                EasyMock.createNiceMock(EventStreamHandler.class));

        EasyMock.expect(executorService.submit(EasyMock.isA(Runnable.class))).andReturn(null);

        EasyMock.replay(context);
        EasyMock.replay(definition);
        EasyMock.replay(manager, addressCache, headHandler, stream, executorService, monitor, connection, allocator, handlerFactory, info, managementService);

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        metadata.setChannelName("endpoint");

        broker.subscribe(URI.create("subscriber"), metadata, connection, getClass().getClassLoader());
        broker.unsubscribe(URI.create("subscriber"), metadata);

        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        EasyMock.verify(manager, addressCache, stream, headHandler, executorService, monitor, connection, allocator, handlerFactory, info, managementService);
    }

    public void testConnectRelease() throws Exception {
        Port port = EasyMock.createMock(Port.class);
        EasyMock.expect(port.getNumber()).andReturn(9090).anyTimes();

        EasyMock.expect(allocator.allocate("channel", "zmq")).andReturn(port);
        allocator.release("channel");
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime");
        EasyMock.expect(info.getZoneName()).andReturn("zone1");

        EventStream stream = EasyMock.createMock(EventStream.class);
        EasyMock.expect(stream.getDefinition()).andReturn(new PhysicalEventStreamDefinition("test"));
        stream.addHandler(EasyMock.isA(EventStreamHandler.class));
        EasyMock.expectLastCall().times(2);
        ChannelConnection connection = EasyMock.createMock(ChannelConnection.class);
        EasyMock.expect(connection.getEventStream()).andReturn(stream);

        addressCache.publish(EasyMock.isA(AddressAnnouncement.class));
        EasyMock.expectLastCall().times(2);

        EasyMock.expect(handlerFactory.createHandler(EasyMock.isA(DataType.class), EasyMock.isA(DataType.class), EasyMock.isA(ClassLoader.class))).andReturn(
                EasyMock.createNiceMock(EventStreamHandler.class));

        manager.reserve(EasyMock.isA(String.class));
        EasyMock.expect(manager.getContext()).andReturn(context);
        manager.release(EasyMock.isA(String.class));

        EasyMock.expect(context.socket(1)).andReturn(EasyMock.createNiceMock(ZMQ.Socket.class));

        port.bind(Port.TYPE.TCP);

        EasyMock.replay(context);
        EasyMock.replay(manager, addressCache, executorService, monitor, connection, allocator, handlerFactory, info, stream, port, managementService);

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        metadata.setChannelName("channel");

        broker.connect("id", metadata, true, connection, getClass().getClassLoader());
        broker.release("id", metadata);

        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        EasyMock.verify(manager, addressCache, executorService, monitor, connection, allocator, handlerFactory, info, stream, port, managementService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = EasyMock.createMock(ZMQ.Context.class);
        manager = EasyMock.createMock(ContextManager.class);

        connection = EasyMock.createMock(ChannelConnection.class);

        addressCache = EasyMock.createMock(AddressCache.class);

        executorService = EasyMock.createMock(ExecutorService.class);

        allocator = EasyMock.createMock(PortAllocator.class);
        info = EasyMock.createMock(HostInfo.class);
        monitor = EasyMock.createNiceMock(MessagingMonitor.class);

        managementService = EasyMock.createNiceMock(ZeroMQManagementService.class);

        EventService eventService = EasyMock.createNiceMock(EventService.class);

        handlerFactory = EasyMock.createMock(TransformerHandlerFactory.class);

        broker = new ZeroMQPubSubBrokerImpl(manager, addressCache, allocator, handlerFactory, managementService, eventService, executorService, info, monitor);

    }
}

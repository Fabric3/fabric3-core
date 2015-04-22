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
package org.fabric3.binding.zeromq.runtime.broker;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.zeromq.runtime.JDK7WorkaroundHelper;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.OneWaySender;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.PhysicalOperation;
import org.fabric3.spi.runtime.event.EventService;
import org.zeromq.ZMQ;

/**
 *
 */
public class ZeroMQWireBrokerImplTestCase extends TestCase {
    private static final ServiceEntry ENTRY = new ServiceEntry("wire", "10.10.10.1", 1234, "tcp");

    private ContextManager manager;
    private DiscoveryAgent discoveryAgent;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private ZMQ.Context context;
    private ZeroMQWireBrokerImpl broker;
    private PortAllocator allocator;
    private TransformerInterceptorFactory interceptorFactory;
    private HostInfo info;
    private ZeroMQManagementService managementService;
    private ZeroMQMetadata metadata;

    public void testConnectToReceiverRelease() throws Exception {

        EasyMock.expect(info.getRuntimeName()).andReturn("runtime");
        EasyMock.expect(info.getZoneName()).andReturn("zone1");

        discoveryAgent.register(EasyMock.isA(ServiceEntry.class));
        EasyMock.expectLastCall();
        discoveryAgent.unregisterService("wire");

        Port port = EasyMock.createMock(Port.class);
        EasyMock.expect(port.getNumber()).andReturn(1099).anyTimes();

        EasyMock.expect(allocator.allocate("wire", "zmq")).andReturn(port);
        allocator.release("wire");

        Interceptor transformInterceptor = EasyMock.createMock(Interceptor.class);

        //noinspection unchecked
        EasyMock.expect(interceptorFactory.createInterceptor(EasyMock.isA(PhysicalOperation.class),
                                                             EasyMock.isA(List.class),
                                                             EasyMock.isA(List.class),
                                                             EasyMock.isA(ClassLoader.class),
                                                             EasyMock.isA(ClassLoader.class))).andReturn(transformInterceptor);

        EasyMock.expect(executorService.submit(EasyMock.isA(Runnable.class))).andReturn(null);

        EasyMock.replay(manager, discoveryAgent, executorService, monitor, allocator, info, managementService, interceptorFactory);

        PhysicalOperation definition = new PhysicalOperation();
        definition.setOneWay(true);

        Interceptor interceptor = EasyMock.createMock(Interceptor.class);
        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(definition).atLeastOnce();
        chain.addInterceptor(EasyMock.isA(Interceptor.class));
        chain.addInterceptor(EasyMock.isA(Interceptor.class));

        EasyMock.expect(chain.getHeadInterceptor()).andReturn(interceptor).atLeastOnce();

        List<InvocationChain> chains = Collections.singletonList(chain);
        EasyMock.replay(chain, interceptor, port);

        broker.connectToReceiver(URI.create("wire"), chains, metadata, getClass().getClassLoader());
        broker.releaseReceiver(URI.create("wire"));

        EasyMock.verify(manager, discoveryAgent, executorService, monitor, allocator, info, chain, interceptor, port, managementService, interceptorFactory);
    }

    public void testConnectToSenderRelease() throws Exception {

        EasyMock.expect(discoveryAgent.getServiceEntries("wire")).andReturn(Collections.singletonList(ENTRY));
        discoveryAgent.registerServiceListener(EasyMock.eq("wire"), EasyMock.isA(OneWaySender.class));
        EasyMock.expectLastCall();

        Interceptor transformInterceptor = EasyMock.createMock(Interceptor.class);

        //noinspection unchecked
        EasyMock.expect(interceptorFactory.createInterceptor(EasyMock.isA(PhysicalOperation.class),
                                                             EasyMock.isA(List.class),
                                                             EasyMock.isA(List.class),
                                                             EasyMock.isA(ClassLoader.class),
                                                             EasyMock.isA(ClassLoader.class))).andReturn(transformInterceptor);

        EasyMock.replay(context);
        EasyMock.replay(manager, discoveryAgent, executorService, monitor, allocator, info, managementService, interceptorFactory);

        PhysicalOperation definition = new PhysicalOperation();
        definition.setOneWay(true);

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(definition).atLeastOnce();
        chain.addInterceptor(EasyMock.isA(Interceptor.class));
        EasyMock.expectLastCall().atLeastOnce();

        List<InvocationChain> chains = Collections.singletonList(chain);
        EasyMock.replay(chain);

        broker.connectToSender("id", URI.create("wire"), chains, metadata, getClass().getClassLoader());
        broker.releaseSender("id", URI.create("wire"));

        JDK7WorkaroundHelper.workaroundLinuxJDK7Assertion(context);
        EasyMock.verify(manager, discoveryAgent, executorService, monitor, allocator, info, chain, managementService, interceptorFactory);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = EasyMock.createMock(ZMQ.Context.class);
        manager = EasyMock.createMock(ContextManager.class);

        discoveryAgent = EasyMock.createMock(DiscoveryAgent.class);

        executorService = EasyMock.createMock(ExecutorService.class);

        allocator = EasyMock.createMock(PortAllocator.class);

        info = EasyMock.createMock(HostInfo.class);

        interceptorFactory = EasyMock.createMock(TransformerInterceptorFactory.class);

        monitor = EasyMock.createNiceMock(MessagingMonitor.class);

        managementService = EasyMock.createNiceMock(ZeroMQManagementService.class);

        EventService eventService = EasyMock.createNiceMock(EventService.class);
        broker = new ZeroMQWireBrokerImpl(manager,
                                          discoveryAgent,
                                          allocator,
                                          executorService,
                                          managementService,
                                          eventService,
                                          interceptorFactory,
                                          info,
                                          monitor);

        metadata = new ZeroMQMetadata();

    }
}

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
package org.fabric3.fabric.federation.addressing;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.federation.addressing.AddressMonitor;
import org.fabric3.spi.federation.addressing.AddressRequest;
import org.fabric3.spi.federation.addressing.AddressUpdate;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.runtime.event.EventService;

/**
 *
 */
public class FederatedAddressCacheImplTestCase extends TestCase {
    private static final String CHANNEL = "F3AddressChannel.domain";
    private static final Port PORT = new Port() {
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

    };

    private static final SocketAddress ADDRESS1 = new SocketAddress("runtime", "zone", "tcp", "10.10.10.1", PORT);
    private static final SocketAddress ADDRESS2 = new SocketAddress("runtime2", "zone", "tcp", "10.10.10.2", PORT);

    private HostInfo info;
    private NodeTopologyService topologyService;
    private EventService eventService;
    private Executor executor;
    private AddressMonitor monitor;

    @SuppressWarnings({"unchecked"})
    public void testOnLeave() throws Exception {

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        topologyService.sendAsynchronous(CHANNEL, announcement);

        AddressListener listener = EasyMock.createMock(AddressListener.class);
        listener.onUpdate(EasyMock.isA(List.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(info, topologyService, listener);

        AddressCacheImpl cache = new AddressCacheImpl(executor, eventService, info, monitor);
        cache.setTopologyService(topologyService);
        cache.subscribe("test", listener);

        cache.publish(announcement);

        cache.onLeave("runtime");
        TestCase.assertTrue(cache.getActiveAddresses("test").isEmpty());

        EasyMock.verify(listener);
    }

    @SuppressWarnings({"unchecked"})
    public void testSendAddressAnnouncement() throws Exception {
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();
        EasyMock.replay(info);
        AddressCacheImpl cache = new AddressCacheImpl(executor, eventService, info, monitor);
        cache.setTopologyService(topologyService);
        topologyService.sendAsynchronous(EasyMock.eq(CHANNEL), EasyMock.isA(AddressAnnouncement.class));
        EasyMock.replay(topologyService);

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);

        TestCase.assertEquals(1, cache.getActiveAddresses("test").size());
        EasyMock.verify(info, topologyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testReceiveAddressAnnouncement() throws Exception {
        EasyMock.replay(info);

        AddressCacheImpl cache = new AddressCacheImpl(executor, eventService, info, monitor);
        cache.setTopologyService(topologyService);
        EasyMock.replay(topologyService);

        AddressAnnouncement announcement2 = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS2);
        cache.onMessage(announcement2);

        TestCase.assertEquals(1, cache.getActiveAddresses("test").size());
        EasyMock.verify(info, topologyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testSendAddressUpdate() throws Exception {
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime").atLeastOnce();
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).atLeastOnce();
        EasyMock.replay(info);
        AddressCacheImpl cache = new AddressCacheImpl(executor, eventService, info, monitor);
        cache.setTopologyService(topologyService);

        topologyService.sendAsynchronous(EasyMock.eq(CHANNEL), EasyMock.isA(AddressAnnouncement.class));

        topologyService.sendAsynchronous(EasyMock.eq("runtime2"), EasyMock.eq(CHANNEL), EasyMock.isA(AddressUpdate.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

            public Object answer() throws Throwable {

                // verify that an announcement for runtime2 is not included
                AddressUpdate update = (AddressUpdate) EasyMock.getCurrentArguments()[2];
                assertEquals(1, update.getAnnouncements().size());
                assertEquals("runtime", update.getAnnouncements().get(0).getAddress().getRuntimeName());
                return null;
            }
        });
        EasyMock.replay(topologyService);

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);

        AddressAnnouncement announcement2 = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS2);
        cache.onMessage(announcement2);

        cache.onMessage(new AddressRequest("runtime2"));

        EasyMock.verify(info, topologyService);

    }

    protected void setUp() throws Exception {
        super.setUp();
        eventService = EasyMock.createNiceMock(EventService.class);
        executor = EasyMock.createNiceMock(Executor.class);
        monitor = EasyMock.createNiceMock(AddressMonitor.class);
        info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain")).atLeastOnce();
        topologyService = EasyMock.createMock(NodeTopologyService.class);
    }
}

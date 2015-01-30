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
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.federation.addressing.AddressMonitor;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.runtime.event.EventService;

/**
 *
 */
public class AddressCacheImplTestCase extends TestCase {
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
    private static final SocketAddress ADDRESS2 = new SocketAddress("runtime", "zone", "tcp", "10.10.10.2", PORT);

    private AddressCacheImpl cache;

    @SuppressWarnings({"unchecked"})
    public void testPublishSubscribe() throws Exception {
        AddressListener listener = EasyMock.createMock(AddressListener.class);
        listener.onUpdate(EasyMock.isA(List.class));
        EasyMock.replay(listener);

        cache.subscribe("test", listener);

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);

        EasyMock.verify(listener);
    }

    @SuppressWarnings({"unchecked"})
    public void testUnsubscribe() throws Exception {
        AddressListener listener = EasyMock.createMock(AddressListener.class);
        EasyMock.expect(listener.getId()).andReturn("id");
        listener.onUpdate(EasyMock.isA(List.class));
        EasyMock.replay(listener);

        cache.subscribe("test", listener);

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);

        cache.unsubscribe("test", "id");

        cache.publish(announcement);

        EasyMock.verify(listener);
    }

    public void testGetAddresses() throws Exception {
        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);
        TestCase.assertEquals(1, cache.getActiveAddresses("test").size());

        announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS2);
        cache.publish(announcement);
        TestCase.assertEquals(2, cache.getActiveAddresses("test").size());

        announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.REMOVED, ADDRESS2);
        cache.publish(announcement);
        TestCase.assertEquals(1, cache.getActiveAddresses("test").size());
        TestCase.assertEquals(ADDRESS1, cache.getActiveAddresses("test").get(0));
    }

    public void setUp() throws Exception {
        super.setUp();
        Executor executor = EasyMock.createNiceMock(Executor.class);
        EventService eventService = EasyMock.createNiceMock(EventService.class);
        HostInfo hostInfo = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(hostInfo.getDomain()).andReturn(URI.create("fabric3://domain"));
        AddressMonitor monitor = EasyMock.createNiceMock(AddressMonitor.class);
        EasyMock.replay(hostInfo);

        cache = new AddressCacheImpl(executor, eventService, hostInfo, monitor);
    }
}

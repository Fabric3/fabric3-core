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
package org.fabric3.fabric.federation.addressing;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Executor;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.federation.addressing.AddressMonitor;
import org.fabric3.spi.federation.addressing.AddressRequest;
import org.fabric3.spi.federation.addressing.AddressUpdate;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.spi.host.Port;

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
    private ParticipantTopologyService topologyService;
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
        cache.setParticipantTopologyService(topologyService);
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
        cache.setParticipantTopologyService(topologyService);
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
        cache.setParticipantTopologyService(topologyService);
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
        cache.setParticipantTopologyService(topologyService);

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
        topologyService = EasyMock.createMock(ParticipantTopologyService.class);
    }
}

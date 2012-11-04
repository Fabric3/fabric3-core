/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.zeromq.runtime.federation;

import java.net.URI;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.host.Port;

/**
 *
 */
public class FederatedAddressCacheTestCase extends TestCase {
    private static final String ZEROMQ_CHANNEL = "ZeroMQChannel.domain";
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

    private static final SocketAddress ADDRESS1 = new SocketAddress("runtime", "tcp", "10.10.10.1", PORT);
    private static final SocketAddress ADDRESS2 = new SocketAddress("runtime2", "tcp", "10.10.10.2", PORT);


    private HostInfo info;
    private ZoneTopologyService topologyService;

    public void testInitDestroy() throws Exception {
        topologyService.openChannel(EasyMock.eq(ZEROMQ_CHANNEL), (String) EasyMock.isNull(), EasyMock.isA(MessageReceiver.class));
        topologyService.register(EasyMock.isA(TopologyListener.class));
        topologyService.sendAsynchronous(EasyMock.eq(ZEROMQ_CHANNEL), EasyMock.isA(AddressRequest.class));
        topologyService.deregister(EasyMock.isA(TopologyListener.class));
        topologyService.closeChannel(ZEROMQ_CHANNEL);
        EasyMock.replay(info, topologyService);

        FederatedAddressCache cache = new FederatedAddressCache(topologyService, info);
        cache.init();
        cache.destroy();

        EasyMock.verify(topologyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testOnLeave() throws Exception {

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        topologyService.sendAsynchronous(ZEROMQ_CHANNEL, announcement);

        AddressListener listener = EasyMock.createMock(AddressListener.class);
        listener.onUpdate(EasyMock.isA(List.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(info, topologyService, listener);

        FederatedAddressCache cache = new FederatedAddressCache(topologyService, info);
        cache.subscribe("test", listener);

        cache.publish(announcement);

        cache.onLeave("runtime");
        assertTrue(cache.getActiveAddresses("test").isEmpty());

        EasyMock.verify(listener);
    }

    @SuppressWarnings({"unchecked"})
    public void testSendAddressAnnouncement() throws Exception {
        EasyMock.replay(info);
        FederatedAddressCache cache = new FederatedAddressCache(topologyService, info);
        topologyService.sendAsynchronous(EasyMock.eq(ZEROMQ_CHANNEL), EasyMock.isA(AddressAnnouncement.class));
        EasyMock.replay(topologyService);

        AddressAnnouncement announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS1);
        cache.publish(announcement);

        assertEquals(1, cache.getActiveAddresses("test").size());
        EasyMock.verify(info, topologyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testReceiveAddressAnnouncement() throws Exception {
        EasyMock.replay(info);

        FederatedAddressCache cache = new FederatedAddressCache(topologyService, info);
        EasyMock.replay(topologyService);

        AddressAnnouncement announcement2 = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS2);
        cache.onMessage(announcement2);

        assertEquals(1, cache.getActiveAddresses("test").size());
        EasyMock.verify(info, topologyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testSendAddressUpdate() throws Exception {
        EasyMock.expect(info.getRuntimeName()).andReturn("runtime").atLeastOnce();
        EasyMock.replay(info);
        FederatedAddressCache cache = new FederatedAddressCache(topologyService, info);

        topologyService.sendAsynchronous(EasyMock.eq(ZEROMQ_CHANNEL), EasyMock.isA(AddressAnnouncement.class));

        topologyService.sendAsynchronous(EasyMock.eq("runtime2"), EasyMock.eq(ZEROMQ_CHANNEL), EasyMock.isA(AddressUpdate.class));
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        info = EasyMock.createNiceMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://domain")).atLeastOnce();
        topologyService = EasyMock.createMock(ZoneTopologyService.class);
    }
}

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
package org.fabric3.binding.zeromq.runtime.federation;

import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.host.Port;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class LocalAddressCacheTestCase extends TestCase {
    private static final Port PORT = new Port(){
        public String getName() {
            return null;
        }

        public int getNumber() {
            return 1061;
        }

        public void releaseLock() {

        }
    };
    private static final SocketAddress ADDRESS1 = new SocketAddress("runtime", "tcp", "10.10.10.1", PORT);
    private static final SocketAddress ADDRESS2 = new SocketAddress("runtime", "tcp", "10.10.10.2", PORT);

    private LocalAddressCache cache = new LocalAddressCache();

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
        assertEquals(1, cache.getActiveAddresses("test").size());

        announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.ACTIVATED, ADDRESS2);
        cache.publish(announcement);
        assertEquals(2, cache.getActiveAddresses("test").size());

        announcement = new AddressAnnouncement("test", AddressAnnouncement.Type.REMOVED, ADDRESS2);
        cache.publish(announcement);
        assertEquals(1, cache.getActiveAddresses("test").size());
        assertEquals(ADDRESS1, cache.getActiveAddresses("test").get(0));
    }

}

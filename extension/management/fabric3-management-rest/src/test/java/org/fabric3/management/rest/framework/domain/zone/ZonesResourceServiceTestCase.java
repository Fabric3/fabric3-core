/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.management.rest.framework.domain.zone;

import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.EndpointConstants;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;

/**
 *
 */
public class ZonesResourceServiceTestCase extends TestCase {
    private ZonesResourceService service;
    private HttpServletRequest request;
    private HostInfo info;
    private AddressCache addressCache;
    private Port port;

    public void testDistributedGetZones() throws Exception {
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE);

        EasyMock.expect(port.getNumber()).andReturn(8080);
        SocketAddress address = new SocketAddress("runtime1", "zone", "http", "localhost", port);
        List<SocketAddress> addresses = Collections.singletonList(address);

        EasyMock.expect(addressCache.getActiveAddresses(EasyMock.eq(EndpointConstants.HTTP_SERVER))).andReturn(addresses);

        EasyMock.replay(request, info, addressCache, port);

        Set<Link> links = service.getZones(request);
        Link link = links.iterator().next();
        assertEquals(new URL("http://localhost:8080/management/zone"), link.getHref());
        EasyMock.verify(request, info, addressCache, port);
    }

    public void testLocalGetZones() throws Exception {
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);

        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/domain/zones")).atLeastOnce();
        EasyMock.replay(request, info, addressCache);

        Set<Link> links = service.getZones(request);
        Link link = links.iterator().next();
        assertEquals(new URL("http://localhost/management/zone"), link.getHref());
        EasyMock.verify(request, info, addressCache);
    }

    protected void setUp() throws Exception {
        super.setUp();

        info = EasyMock.createMock(HostInfo.class);

        addressCache = EasyMock.createMock(AddressCache.class);

        port = EasyMock.createMock(Port.class);

        service = new ZonesResourceService(addressCache, info);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

}

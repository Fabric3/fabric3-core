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

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.topology.DomainTopologyService;
import org.fabric3.spi.federation.topology.RuntimeInstance;
import org.fabric3.spi.federation.topology.Zone;

import static org.fabric3.spi.federation.topology.FederationConstants.HTTP_HOST_METADATA;
import static org.fabric3.spi.federation.topology.FederationConstants.HTTP_PORT_METADATA;

/**
 *
 */
public class ZonesResourceServiceTestCase extends TestCase {
    private ZonesResourceService service;
    private HttpServletRequest request;

    public void testDistributedGetZones() throws Exception {
        DomainTopologyService topologyService = EasyMock.createMock(DomainTopologyService.class);
        service.setTopologyService(topologyService);

        Map<String, Serializable> metadata = new HashMap<String, Serializable>();
        metadata.put(HTTP_HOST_METADATA, "localhost");
        metadata.put(HTTP_PORT_METADATA, 8080);

        RuntimeInstance instance = new RuntimeInstance("runtime1", metadata);
        List<RuntimeInstance> instances = Collections.singletonList(instance);

        Zone zone = new Zone("zone1", instances);
        EasyMock.expect(topologyService.getZones()).andReturn(Collections.singleton(zone));

        EasyMock.replay( topologyService, request);

        Set<Link> links = service.getZones(request);
        Link link = links.iterator().next();
        assertEquals(new URL("http://localhost:8080/management/zone"), link.getHref());
        EasyMock.verify(topologyService, request);
    }

    public void testLocalGetZones() throws Exception {
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/domain/zones")).atLeastOnce();
        EasyMock.replay(request);

        Set<Link> links = service.getZones(request);
        Link link = links.iterator().next();
        assertEquals(new URL("http://localhost/management/zone"), link.getHref());
        EasyMock.verify(request);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        service = new ZonesResourceService();

        request = EasyMock.createMock(HttpServletRequest.class);
    }

}

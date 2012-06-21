/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.management.rest.framework.domain.runtime;

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

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Link;
import org.fabric3.spi.federation.DomainTopologyService;
import org.fabric3.spi.federation.RuntimeInstance;
import org.fabric3.spi.federation.Zone;

import static org.fabric3.spi.federation.FederationConstants.HTTP_HOST_METADATA;
import static org.fabric3.spi.federation.FederationConstants.HTTP_PORT_METADATA;

/**
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
public class RuntimesResourceServiceTestCase extends TestCase {

    private HostInfo info;
    private RuntimesResourceService service;
    private HttpServletRequest request;

    public void testDistributedGetRuntimes() throws Exception {
        DomainTopologyService topologyService = EasyMock.createMock(DomainTopologyService.class);
        service.setTopologyService(topologyService);

        Map<String, Serializable> metadata = new HashMap<String, Serializable>();
        metadata.put(HTTP_HOST_METADATA, "localhost");
        metadata.put(HTTP_PORT_METADATA, 8080);

        RuntimeInstance instance = new RuntimeInstance("runtime1", metadata);
        List<RuntimeInstance> instances = Collections.singletonList(instance);

        Zone zone = new Zone("zone1", instances);
        EasyMock.expect(topologyService.getZones()).andReturn(Collections.singleton(zone));

        EasyMock.replay(info, topologyService, request);

        Set<Link> links = service.getRuntimes(request);
        Link link = links.iterator().next();
        assertEquals("runtime1", link.getName());
        URL url = new URL("http://localhost:8080/management/runtime");
        assertEquals(url, link.getHref());
        EasyMock.verify(info, topologyService, request);
    }

    public void testLocalGetRuntimes() throws Exception {
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/domain/contributions")).atLeastOnce();

        EasyMock.replay(info, request);

        Set<Link> links = service.getRuntimes(request);
        Link link = links.iterator().next();
        assertEquals("vm", link.getName());
        URL url = new URL("http:/localhost/management/runtime");
        assertEquals(url, link.getHref());
        EasyMock.verify(info, request);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        info = EasyMock.createMock(HostInfo.class);
        service = new RuntimesResourceService(info);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

}

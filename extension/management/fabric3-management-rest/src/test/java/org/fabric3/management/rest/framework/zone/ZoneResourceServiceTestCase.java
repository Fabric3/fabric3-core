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
package org.fabric3.management.rest.framework.zone;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.Role;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.runtime.ManagementMonitor;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;

/**
 *
 */
public class ZoneResourceServiceTestCase extends TestCase {

    private ResourceHost host;
    private HostInfo info;
    private ZoneResourceService service;
    private HttpServletRequest request;

    public void testDistributedGetZoneResource() throws Exception {
        ParticipantTopologyService topologyService = EasyMock.createMock(ParticipantTopologyService.class);
        EasyMock.expect(topologyService.getZoneLeaderName()).andReturn("runtime1");
        service.setTopologyService(topologyService);

        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/zone")).atLeastOnce();

        EasyMock.expect(info.getRuntimeName()).andReturn("runtime1");
        EasyMock.replay(info, topologyService, request);

        Resource resource = service.getZoneResource(request);
        Map<String, Object> properties = resource.getProperties();
        assertEquals("runtime1", properties.get("name"));
        assertEquals("runtime1", properties.get("leader"));
        Link link = (Link) properties.get("runtime");
        assertEquals("http:/localhost/management/zone/runtime", link.getHref().toString());
        EasyMock.verify(info, topologyService, request);
    }


    public void testLocalGetZoneResource() throws Exception {

        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/zone")).atLeastOnce();

        EasyMock.expect(info.getRuntimeName()).andReturn("runtime1").atLeastOnce();
        EasyMock.replay(info, request);

        Resource resource = service.getZoneResource(request);
        Map<String, Object> properties = resource.getProperties();
        assertEquals("runtime1", properties.get("name"));
        assertEquals("runtime1", properties.get("leader"));
        Link link = (Link) properties.get("runtime");
        assertEquals("http:/localhost/management/zone/runtime", link.getHref().toString());
        EasyMock.verify(info, request);
    }

    public void testGetZoneRuntimeResource() throws Exception {
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/management/zone")).atLeastOnce();

        EasyMock.expect(host.isPathRegistered("/zone/runtime/resource1", Verb.GET)).andReturn(false);
        host.register(EasyMock.isA(ResourceMapping.class));

        EasyMock.replay(info, request, host);

        ResourceMapping mapping = createMapping();
        service.onRootResourceExport(mapping);
        Resource resource = service.getZoneRuntimeResource(request);
        Map<String, Object> properties = resource.getProperties();
        Link link = (Link) properties.get("resource1");
        assertEquals("http:/localhost/management/zone/resource1", link.getHref().toString());
        EasyMock.verify(info, request, host);
    }

    public void testDestroy() throws Exception {
        host.register(EasyMock.isA(ResourceMapping.class));
        host.unregister("/zone/runtime/resource1");

        EasyMock.replay(info, request, host);

        ResourceMapping mapping = createMapping();
        service.onSubResourceExport(mapping);
        service.destroy();
        EasyMock.verify(info, request, host);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        EasyMock.replay(monitor);

        host = EasyMock.createMock(ResourceHost.class);
        info = EasyMock.createMock(HostInfo.class);
        service = new ZoneResourceService(host, info, monitor);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

    private ResourceMapping createMapping() {
        String path = "/runtime/resource1";
        return new ResourceMapping("resource1", path, path, Verb.GET, null, new Object(), null, Collections.<Role>emptySet());
    }


}

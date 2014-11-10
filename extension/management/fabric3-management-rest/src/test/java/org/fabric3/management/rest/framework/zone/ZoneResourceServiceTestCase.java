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
 */
package org.fabric3.management.rest.framework.zone;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.Role;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
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
        ZoneResourceMonitor monitor = EasyMock.createNiceMock(ZoneResourceMonitor.class);
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

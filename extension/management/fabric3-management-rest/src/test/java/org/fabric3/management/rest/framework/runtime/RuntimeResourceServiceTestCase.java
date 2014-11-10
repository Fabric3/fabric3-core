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
package org.fabric3.management.rest.framework.runtime;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.Role;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;

/**
 *
 */
public class RuntimeResourceServiceTestCase extends TestCase {

    private HostInfo info;
    private RuntimeResourceService service;
    private HttpServletRequest request;

    public void testGetResource() throws Exception {
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http:/localhost/managementruntime")).atLeastOnce();
        EasyMock.expect(info.getRuntimeName()).andReturn("vm");
        URI domainUri = URI.create("fabric3://domain");
        EasyMock.expect(info.getDomain()).andReturn(domainUri);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);

        EasyMock.replay(info, request);

        Resource resource = service.getResource(request);
        Map<String, Object> properties = resource.getProperties();
        assertEquals("vm", properties.get("name"));
        assertEquals(domainUri, properties.get("domain"));
        assertEquals(RuntimeMode.VM, properties.get("mode"));
        EasyMock.verify(info, request);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        info = EasyMock.createMock(HostInfo.class);
        service = new RuntimeResourceService(info);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

    private ResourceMapping createMapping() {
        String path = "/runtime/resource1";
        return new ResourceMapping("resource1", path, path, Verb.GET, null, new Object(), null, Collections.<Role>emptySet());
    }

}

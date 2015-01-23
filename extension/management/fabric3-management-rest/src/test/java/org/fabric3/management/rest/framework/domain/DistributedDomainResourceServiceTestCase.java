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
package org.fabric3.management.rest.framework.domain;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.management.rest.model.Resource;

/**
 *
 */
public class DistributedDomainResourceServiceTestCase extends TestCase {
    private DistributedDomainResourceService service;
    private HostInfo info;

    public void testLocalGetDomainResource() throws Exception {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/domain")).atLeastOnce();
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();
        EasyMock.replay(info, request);

        Resource resource = service.getDomainResource(request);
        Map<String, Object> properties = resource.getProperties();
        assertNotNull(properties.get("contributions"));
        assertNotNull(properties.get("deployments"));
        assertNotNull(properties.get("components"));

        EasyMock.verify(info, request);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        info = EasyMock.createMock(HostInfo.class);
        service = new DistributedDomainResourceService(info);
    }

}

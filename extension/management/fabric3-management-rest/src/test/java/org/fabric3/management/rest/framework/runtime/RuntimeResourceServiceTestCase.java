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
package org.fabric3.management.rest.framework.runtime;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.Role;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.runtime.ManagementMonitor;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;

/**
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
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
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        EasyMock.replay(monitor);

        info = EasyMock.createMock(HostInfo.class);
        service = new RuntimeResourceService(info);

        request = EasyMock.createMock(HttpServletRequest.class);
    }

    private ResourceMapping createMapping() {
        String path = "/runtime/resource1";
        return new ResourceMapping("resource1", path, path, Verb.GET, null, new Object(), null, Collections.<Role>emptySet());
    }


}

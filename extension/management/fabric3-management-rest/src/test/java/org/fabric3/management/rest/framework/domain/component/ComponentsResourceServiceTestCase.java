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
package org.fabric3.management.rest.framework.domain.component;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class ComponentsResourceServiceTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testGetComponents() {
        URI domainUri = URI.create("fabric3://domain");
        LogicalCompositeComponent domain = new LogicalCompositeComponent(domainUri, null, null);

        URI compositeUri = URI.create("fabric3://domain/foo/bar");
        LogicalCompositeComponent composite = new LogicalCompositeComponent(compositeUri, null, domain);
        URI componentUri = URI.create("fabric3://domain/foo/bar/baz");
        LogicalComponent component = new LogicalComponent(componentUri, null, composite);
        composite.addComponent(component);

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.expect(lcm.getComponent(EasyMock.eq(compositeUri)));
        EasyMock.expectLastCall().andReturn(composite);
        EasyMock.replay(lcm);

        ComponentsResourceService service = new ComponentsResourceService(lcm);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/components/foo/bar"));
        EasyMock.expect(request.getScheme()).andReturn("http");
        EasyMock.expect(request.getServerName()).andReturn("localhost");
        EasyMock.expect(request.getServerPort()).andReturn(8080);
        EasyMock.expect(request.getPathInfo()).andReturn("/domain/components/foo/bar");
        EasyMock.replay(request);

        Response response = service.getComponents(request);
        assertEquals(HttpStatus.OK, response.getStatus());

        CompositeResource resource = (CompositeResource) response.getEntity();
        assertEquals(compositeUri, resource.getUri());
        assertEquals(1, resource.getComponents().size());
        assertEquals(componentUri, resource.getComponents().get(0).getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testGetComponentsAtomic() {
        URI domainUri = URI.create("fabric3://domain");
        LogicalCompositeComponent domain = new LogicalCompositeComponent(domainUri, null, null);

        URI componentUri = URI.create("fabric3://domain/foo/bar/baz");
        LogicalComponent component = new LogicalComponent(componentUri, null, null);

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.expect(lcm.getComponent(EasyMock.eq(componentUri)));
        EasyMock.expectLastCall().andReturn(component);
        EasyMock.replay(lcm);

        ComponentsResourceService service = new ComponentsResourceService(lcm);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/components/foo/bar/baz"));
        EasyMock.expect(request.getScheme()).andReturn("http");
        EasyMock.expect(request.getServerName()).andReturn("localhost");
        EasyMock.expect(request.getServerPort()).andReturn(8080);
        EasyMock.expect(request.getPathInfo()).andReturn("/domain/components/foo/bar/baz");
        EasyMock.replay(request);

        Response response = service.getComponents(request);
        assertEquals(HttpStatus.OK, response.getStatus());

        ComponentResource resource = (ComponentResource) response.getEntity();
        assertEquals(componentUri, resource.getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testGetComponentsDomain() {
        URI domainUri = URI.create("fabric3://domain");
        LogicalCompositeComponent domain = new LogicalCompositeComponent(domainUri, null, null);

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.expect(lcm.getComponent(EasyMock.eq(URI.create("/"))));
        EasyMock.expectLastCall().andReturn(domain);
        EasyMock.replay(lcm);

        ComponentsResourceService service = new ComponentsResourceService(lcm);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost/management/components"));
        EasyMock.expect(request.getScheme()).andReturn("http");
        EasyMock.expect(request.getServerName()).andReturn("localhost");
        EasyMock.expect(request.getServerPort()).andReturn(8080);
        EasyMock.expect(request.getPathInfo()).andReturn("");
        EasyMock.replay(request);

        Response response = service.getComponents(request);
        assertEquals(HttpStatus.OK, response.getStatus());

        CompositeResource resource = (CompositeResource) response.getEntity();
        assertEquals(domainUri, resource.getUri());
    }

    @SuppressWarnings({"unchecked"})
    public void testGetComponentsNotFound() {
        URI domainUri = URI.create("fabric3://domain");
        LogicalCompositeComponent domain = new LogicalCompositeComponent(domainUri, null, null);

        URI compositeUri = URI.create("fabric3://domain/foo/bar");

        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.expect(lcm.getComponent(EasyMock.eq(compositeUri)));
        EasyMock.expectLastCall().andReturn(null);
        EasyMock.replay(lcm);

        ComponentsResourceService service = new ComponentsResourceService(lcm);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/domain/components/foo/bar");
        EasyMock.replay(request);

        Response response = service.getComponents(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertNull(response.getEntity());
    }

}

/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.management.rest.framework.domain.component;

import java.net.URI;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Response;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
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

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
package org.fabric3.management.rest.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.spi.DuplicateResourceNameException;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.security.BasicAuthenticator;

/**
 *
 */
public final class ResourceHostImplTestCase extends TestCase {
    private ResourceHostImpl host;
    private Method errorMethod;
    private Method operationMethod;
    private Method parameterizedMethod;

    private Marshaller marshaller;
    private ServletHost servletHost;
    private HttpServletResponse response;

    public void testIsRegisteredInPath() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        host.register(mapping);
        assertTrue(host.isPathRegistered("/foo/bar", Verb.GET));
        assertFalse(host.isPathRegistered("/foo/bar", Verb.DELETE));
    }

    public void testIsRegistered() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        host.register(mapping);
        host.unregisterPath("/foo/bar", Verb.GET);
        assertFalse(host.isPathRegistered("/foo/bar", Verb.GET));
    }

    public void testUnregister() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        ResourceMapping mapping2 = new ResourceMapping("foo", "/foo/bar", "bar", Verb.DELETE, null, null, null, null);
        host.register(mapping);
        host.register(mapping2);
        host.unregister("foo");
        assertFalse(host.isPathRegistered("/foo/bar", Verb.GET));
        assertFalse(host.isPathRegistered("/foo/bar", Verb.DELETE));
    }

    public void testDuplicateRegister() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        ResourceMapping mapping2 = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        host.register(mapping);
        try {
            host.register(mapping2);
            fail();
        } catch (DuplicateResourceNameException e) {
            // expected
        }
    }

    public void testNotFoundParentResource() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        host.register(mapping);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo").anyTimes();
        response.setStatus(404);
        EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(new StringWriter()));

        EasyMock.replay(request, response);

        host.doGet(request, response);
        EasyMock.verify(request, response);
    }

    public void testNotFoundDifferentVerbResource() throws Exception {
        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, null, null, null, null);
        host.register(mapping);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar").anyTimes();
        response.setStatus(404);
        EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(new StringWriter()));

        EasyMock.replay(request, response);

        host.doPost(request, response);
        EasyMock.verify(request, response);
    }

    public void testNotFoundSubResource() throws Exception {
        MockResource resource = EasyMock.createMock(MockResource.class);

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, operationMethod, resource, null, null);
        host.register(mapping);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar/baz").anyTimes();
        response.setStatus(404);
        EasyMock.expect(response.getWriter()).andReturn(new PrintWriter(new StringWriter()));

        EasyMock.replay(request, response);

        host.doGet(request, response);
        EasyMock.verify(request, response);
    }

    public void testResolveParameterizedResource() throws Exception {

        MockResource resource = EasyMock.createMock(MockResource.class);
        EasyMock.expect(resource.parameterized("test")).andReturn("test");

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, parameterizedMethod, resource, null, null);
        host.register(mapping);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar/test").anyTimes();

        EasyMock.expect(marshaller.deserialize(Verb.GET, request, mapping)).andReturn(new Object[]{"test"});
        marshaller.serialize("test", mapping, request, response);

        response.setContentType("application/json");

        EasyMock.replay(request, response, marshaller, resource);

        host.doGet(request, response);
        EasyMock.verify(request, response, marshaller, resource);
    }

    public void testResolveParentParameterizedResource() throws Exception {
        MockResource resource = EasyMock.createMock(MockResource.class);
        EasyMock.expect(resource.parameterized("test")).andReturn("test");

        MockResource subresource = EasyMock.createMock(MockResource.class);

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, parameterizedMethod, resource, null, null);
        host.register(mapping);

        ResourceMapping mapping2 = new ResourceMapping("foo", "/foo/bar/baz", "bar", Verb.GET, parameterizedMethod, resource, null, null);
        host.register(mapping2);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar/test").anyTimes();

        EasyMock.expect(marshaller.deserialize(Verb.GET, request, mapping)).andReturn(new Object[]{"test"});
        marshaller.serialize("test", mapping, request, response);

        response.setContentType("application/json");

        EasyMock.replay(request, response, marshaller, resource, subresource);

        host.doGet(request, response);
        EasyMock.verify(request, response, marshaller, resource, subresource);
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testInvokeErrorResource() throws Exception {
        ResourceException e = new ResourceException(HttpStatus.BAD_REQUEST);
        MockResource resource = EasyMock.createMock(MockResource.class);
        resource.error();
        EasyMock.expectLastCall().andThrow(e);

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.GET, errorMethod, resource, null, null);
        host.register(mapping);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar").anyTimes();
        response.setStatus(HttpStatus.BAD_REQUEST.getCode());

        response.setContentType("application/json");

        EasyMock.expect(marshaller.deserialize(Verb.GET, request, mapping)).andReturn(new Object[]{});
        EasyMock.replay(request, response, marshaller, resource);

        host.doGet(request, response);
        EasyMock.verify(request, response, marshaller, resource);
    }

    public void testStartStop() throws Exception {
        servletHost.registerMapping("/management/*", host);
        EasyMock.expect(servletHost.unregisterMapping("/management/*")).andReturn(host);
        EasyMock.replay(servletHost);

        host.start();
        host.stop();

        EasyMock.verify(servletHost);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        errorMethod = MockResource.class.getMethod("error");
        parameterizedMethod = MockResource.class.getMethod("parameterized", String.class);
        operationMethod = MockResource.class.getMethod("operation");

        response = EasyMock.createMock(HttpServletResponse.class);

        marshaller = EasyMock.createMock(Marshaller.class);
        servletHost = EasyMock.createMock(ServletHost.class);
        BasicAuthenticator authenticator = EasyMock.createMock(BasicAuthenticator.class);
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        host = new ResourceHostImpl(marshaller, servletHost, authenticator, monitor);
    }

    private static interface MockResource {

        void error() throws ResourceException;

        String operation();

        String parameterized(String message);
    }
}

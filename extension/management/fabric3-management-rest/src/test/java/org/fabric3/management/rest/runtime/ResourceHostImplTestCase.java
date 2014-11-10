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

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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.Role;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.security.BasicSecuritySubject;

/**
 *
 */
public class ResourceInvokerTestCase extends TestCase {
    private Method method;
    private TestResource instance;

    @SuppressWarnings({"unchecked"})
    public void testInvokeNoSecurity() throws Exception {
        EasyMock.expect(instance.invoke()).andReturn("test").times(2);
        Set<Role> roles = Collections.emptySet();
        List<ResourceMapping> mappings = createMappings(instance, method, roles);


        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.DISABLED);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, instance);

        Resource resource = invoker.invoke(request);
        List<Link> links = (List<Link>) resource.getProperties().get("links");
        assertEquals(2, links.size());

        Link link1 = links.get(0);
        assertEquals("bar", link1.getName());
        assertEquals("http://localhost/management/service/bar", link1.getHref().toString());

        Link link2 = links.get(1);
        assertEquals("baz", link2.getName());
        assertEquals("http://localhost/management/service/baz", link2.getHref().toString());

        EasyMock.verify(request, instance);
    }

    @SuppressWarnings({"unchecked"})
    public void testInvokeNoSecurityObjectFactory() throws Exception {
        EasyMock.expect(instance.invoke()).andReturn("test").times(2);

        ObjectFactory<TestResource> factory = EasyMock.createMock(ObjectFactory.class);
        EasyMock.expect(factory.getInstance()).andReturn(instance).times(2);

        Set<Role> roles = Collections.emptySet();
        List<ResourceMapping> mappings = createMappings(factory, method, roles);

        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.DISABLED);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, factory, instance);

        Resource resource = invoker.invoke(request);
        List<Link> links = (List<Link>) resource.getProperties().get("links");
        assertEquals(2, links.size());

        Link link1 = links.get(0);
        assertEquals("bar", link1.getName());
        assertEquals("http://localhost/management/service/bar", link1.getHref().toString());

        Link link2 = links.get(1);
        assertEquals("baz", link2.getName());
        assertEquals("http://localhost/management/service/baz", link2.getHref().toString());

        EasyMock.verify(request, factory, instance);
    }

    @SuppressWarnings({"unchecked"})
    public void testInvokeUnAuthorized() throws Exception {
        Set<Role> roles = Collections.singleton(new Role("admin"));
        List<ResourceMapping> mappings = createMappings(instance, method, roles);


        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.AUTHORIZATION);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, instance);

        try {
            invoker.invoke(request);
            fail();
        } catch (ResourceException e) {
            // expected 
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        }


        EasyMock.verify(request, instance);
    }

    @SuppressWarnings({"unchecked"})
    public void testInvokeAuthorized() throws Exception {
        EasyMock.expect(instance.invoke()).andReturn("test").times(2);

        Set<Role> roles = Collections.singleton(new Role("admin"));
        List<ResourceMapping> mappings = createMappings(instance, method, roles);

        WorkContextCache.getThreadWorkContext().setSubject(new BasicSecuritySubject("foo", "bar", roles));

        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.AUTHORIZATION);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, instance);

        invoker.invoke(request);


        EasyMock.verify(request, instance);
    }

    @SuppressWarnings({"unchecked"})
    public void testInvokeAuthenticate() throws Exception {
        EasyMock.expect(instance.invoke()).andReturn("test").times(2);

        Set<Role> roles = Collections.emptySet();
        List<ResourceMapping> mappings = createMappings(instance, method, roles);

        WorkContextCache.getThreadWorkContext().setSubject(new BasicSecuritySubject("foo", "bar", roles));

        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.AUTHENTICATION);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, instance);

        invoker.invoke(request);


        EasyMock.verify(request, instance);
    }

    @SuppressWarnings({"unchecked"})
    public void testInvokeUnAuthorizedNotInRole() throws Exception {
        Set<Role> roles = Collections.singleton(new Role("admin"));
        List<ResourceMapping> mappings = createMappings(instance, method, roles);

        WorkContextCache.getThreadWorkContext().setSubject(new BasicSecuritySubject("foo", "bar", Collections.<Role>emptySet()));

        ResourceInvoker invoker = new ResourceInvoker(mappings, ManagementSecurity.AUTHENTICATION);
        HttpServletRequest request = createRequest();

        EasyMock.replay(request, instance);

        try {
            invoker.invoke(request);
            fail();
        } catch (ResourceException e) {
            // expected
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        }

        EasyMock.verify(request, instance);
    }

    protected void setUp() throws Exception {
        super.setUp();
        method = TestResource.class.getMethod("invoke");
        method.setAccessible(true);

        instance = EasyMock.createMock(TestResource.class);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private List<ResourceMapping> createMappings(Object instance, Method method, Set<Role> roles) {
        ResourceMapping mapping1 = new ResourceMapping("foo", "management/foo/bar", "bar", Verb.GET, method, instance, null, roles);
        ResourceMapping mapping2 = new ResourceMapping("foo", "management/foo/baz", "baz", Verb.GET, method, instance, null, roles);
        List<ResourceMapping> mappings = new ArrayList<>();
        mappings.add(mapping1);
        mappings.add(mapping2);
        return mappings;
    }

    private HttpServletRequest createRequest() {
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        request.getRequestURL();
        EasyMock.expectLastCall().andStubAnswer(new UrlAnswer());
        return request;
    }

    private interface TestResource {

        String invoke();
    }

    private class UrlAnswer implements IAnswer<Object> {

        public StringBuffer answer() throws Throwable {
            return new StringBuffer("http://localhost/management/service");
        }
    }
}

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
import org.fabric3.spi.invocation.WorkContextCache;
import org.fabric3.spi.objectfactory.ObjectFactory;
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
        List<ResourceMapping> mappings = new ArrayList<ResourceMapping>();
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

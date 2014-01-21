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
import java.io.Serializable;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.management.rest.spi.Verb;
import org.fabric3.spi.federation.topology.MessageReceiver;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.spi.federation.topology.TopologyListener;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.security.BasicAuthenticator;

/**
 *
 */
public final class ResourceHostImplReplicationTestCase extends TestCase {
    private ResourceHostImpl host;
    private Method parameterizedMethod;
    private Method httpRequestMethod;
    private Marshaller marshaller;
    private HttpServletResponse response;

    public void testDispatch() throws Exception {
        MockResource instance = EasyMock.createMock(MockResource.class);
        EasyMock.expect(instance.parameterized("test")).andReturn("test");
        EasyMock.replay(instance);

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.POST, parameterizedMethod, instance, null, null);

        host.start();
        host.register(mapping);

        host.dispatch("/foo/bar", Verb.POST, new String[]{"test"});

        EasyMock.verify(instance);
    }

    public void testReplicateChange() throws Exception {
        ParticipantTopologyService topologyService = EasyMock.createMock(ParticipantTopologyService.class);
        topologyService.openChannel(EasyMock.isA(String.class),
                                    (String) EasyMock.isNull(),
                                    EasyMock.isA(MessageReceiver.class),
                                    (TopologyListener) EasyMock.isNull());
        topologyService.sendAsynchronous(EasyMock.isA(String.class), EasyMock.isA((Serializable.class)));
        MockResource instance = EasyMock.createMock(MockResource.class);
        EasyMock.expect(instance.parameterized("test")).andReturn("test");

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar").atLeastOnce();

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.POST, parameterizedMethod, instance, true, null, null);

        EasyMock.expect(marshaller.deserialize(Verb.POST, request, mapping)).andReturn(new String[]{"test"});
        marshaller.serialize("test", mapping, request, response);
        EasyMock.replay(instance, request, response, marshaller, topologyService);

        host.setTopologyService(topologyService);
        host.start();
        host.register(mapping);

        host.doPost(request, response);

        EasyMock.verify(instance, request, response, marshaller, topologyService);
    }

    public void testReplicateHttpServletRequestChange() throws Exception {
        ParticipantTopologyService topologyService = EasyMock.createMock(ParticipantTopologyService.class);
        topologyService.openChannel(EasyMock.isA(String.class),
                                    (String) EasyMock.isNull(),
                                    EasyMock.isA(MessageReceiver.class),
                                    (TopologyListener) EasyMock.isNull());
        topologyService.sendAsynchronous(EasyMock.isA(String.class), EasyMock.isA((Serializable.class)));

        MockResource instance = EasyMock.createMock(MockResource.class);
        instance.parameterized(EasyMock.isA(HttpServletRequest.class));

        HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/foo/bar").atLeastOnce();

        ResourceMapping mapping = new ResourceMapping("foo", "/foo/bar", "bar", Verb.POST, httpRequestMethod, instance, true, null, null);

        EasyMock.expect(marshaller.deserialize(Verb.POST, request, mapping)).andReturn(new Object[]{request});

        EasyMock.replay(instance, request, response, marshaller, topologyService);

        host.setTopologyService(topologyService);
        host.start();
        host.register(mapping);

        host.doPost(request, response);

        EasyMock.verify(instance, request, response, marshaller, topologyService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        parameterizedMethod = MockResource.class.getMethod("parameterized", String.class);
        httpRequestMethod = MockResource.class.getMethod("parameterized", HttpServletRequest.class);

        response = EasyMock.createMock(HttpServletResponse.class);
        response.setContentType("application/json");

        marshaller = EasyMock.createMock(Marshaller.class);
        ServletHost servletHost = EasyMock.createNiceMock(ServletHost.class);
        BasicAuthenticator authenticator = EasyMock.createNiceMock(BasicAuthenticator.class);
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);
        host = new ResourceHostImpl(marshaller, servletHost, authenticator, monitor);
    }

    private static interface MockResource {

        void error() throws ResourceException;

        String parameterized(String message);

        void parameterized(HttpServletRequest request);
    }
}

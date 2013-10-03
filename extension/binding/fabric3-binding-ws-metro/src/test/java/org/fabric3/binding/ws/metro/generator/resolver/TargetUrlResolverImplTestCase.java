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
package org.fabric3.binding.ws.metro.generator.resolver;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.Constants;

import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.spi.federation.topology.DomainTopologyService;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;

/**
 *
 */
public class TargetUrlResolverImplTestCase extends TestCase {
    private LogicalBinding<WsBindingDefinition> binding;

    public void testSingleVMHttp() throws Exception {
        ServletHost servletHost = EasyMock.createMock(ServletHost.class);
        EasyMock.expect(servletHost.getHttpPort()).andReturn(8080);
        EasyMock.replay(servletHost);

        EffectivePolicy policy = createPolicy();

        TargetUrlResolver resolver = new TargetUrlResolverImpl(servletHost, null);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("http://localhost:8080/service", url.toString());
    }

    public void testSingleVMHttps() throws Exception {
        ServletHost servletHost = EasyMock.createMock(ServletHost.class);
        EasyMock.expect(servletHost.getHttpsPort()).andReturn(8989);
        EasyMock.replay(servletHost);

        EffectivePolicy policy = createSecurityPolicy();

        TargetUrlResolver resolver = new TargetUrlResolverImpl(servletHost, null);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("https://localhost:8989/service", url.toString());
    }

    public void testClusterVMHttp() throws Exception {
        DomainTopologyService topologyService = EasyMock.createMock(DomainTopologyService.class);
        EasyMock.expect(topologyService.getTransportMetaData("1", "http")).andReturn("clusteraddress:8080");
        EasyMock.replay(topologyService);

        EffectivePolicy policy = createPolicy();

        TargetUrlResolver resolver = new TargetUrlResolverImpl(null, topologyService);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("http://clusteraddress:8080/service", url.toString());
    }

    public void testClusterVMHttps() throws Exception {
        DomainTopologyService topologyService = EasyMock.createMock(DomainTopologyService.class);
        EasyMock.expect(topologyService.getTransportMetaData("1", "https")).andReturn("clusteraddress:8989");
        EasyMock.replay(topologyService);

        EffectivePolicy policy = createSecurityPolicy();

        TargetUrlResolver resolver = new TargetUrlResolverImpl(null, topologyService);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("https://clusteraddress:8989/service", url.toString());
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WsBindingDefinition definition = new WsBindingDefinition("name", URI.create("service"), null, null, 0);
        LogicalComponent<?> component = new LogicalComponent(null, null, null);
        component.setZone("1");
        LogicalService service = new LogicalService(null, null, component);
        binding = new LogicalBinding<WsBindingDefinition>(definition, service);
    }

    private EffectivePolicy createPolicy() {
        EffectivePolicy policy = EasyMock.createMock(EffectivePolicy.class);
        EasyMock.expect(policy.getProvidedEndpointIntents()).andReturn(Collections.<Intent>emptySet());
        EasyMock.replay(policy);
        return policy;
    }

    private EffectivePolicy createSecurityPolicy() {
        Set<Intent> intents = new HashSet<Intent>();
        QName qname = new QName(Constants.SCA_NS, "confidentiality");
        Intent intent = new Intent(qname, null, null, null, false, null, null, false);
        intents.add(intent);
        EffectivePolicy policy = EasyMock.createMock(EffectivePolicy.class);
        EasyMock.expect(policy.getProvidedEndpointIntents()).andReturn(intents);
        EasyMock.replay(policy);
        return policy;
    }

}

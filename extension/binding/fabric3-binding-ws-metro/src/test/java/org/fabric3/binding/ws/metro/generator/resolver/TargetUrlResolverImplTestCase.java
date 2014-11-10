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
package org.fabric3.binding.ws.metro.generator.resolver;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.oasisopen.sca.Constants;

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

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);
        EasyMock.replay(info);

        TargetUrlResolver resolver = new TargetUrlResolverImpl(servletHost, info);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("http://localhost:8080/service", url.toString());
        EasyMock.verify(info);
    }

    public void testSingleVMHttps() throws Exception {
        ServletHost servletHost = EasyMock.createMock(ServletHost.class);
        EasyMock.expect(servletHost.getHttpsPort()).andReturn(8989);
        EasyMock.replay(servletHost);

        EffectivePolicy policy = createSecurityPolicy();

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);
        EasyMock.replay(info);

        TargetUrlResolver resolver = new TargetUrlResolverImpl(servletHost, info);

        URL url = resolver.resolveUrl(binding, policy);
        assertEquals("https://localhost:8989/service", url.toString());
        EasyMock.verify(info);
    }

    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        WsBindingDefinition definition = new WsBindingDefinition("name", URI.create("service"), null, null, 0);
        LogicalComponent<?> component = new LogicalComponent(null, null, null);
        component.setZone("1");
        LogicalService service = new LogicalService(null, null, component);
        binding = new LogicalBinding<>(definition, service);
    }

    private EffectivePolicy createPolicy() {
        EffectivePolicy policy = EasyMock.createMock(EffectivePolicy.class);
        EasyMock.expect(policy.getProvidedEndpointIntents()).andReturn(Collections.<Intent>emptySet());
        EasyMock.replay(policy);
        return policy;
    }

    private EffectivePolicy createSecurityPolicy() {
        Set<Intent> intents = new HashSet<>();
        QName qname = new QName(Constants.SCA_NS, "confidentiality");
        Intent intent = new Intent(qname, null, null, null, false, null, null, false);
        intents.add(intent);
        EffectivePolicy policy = EasyMock.createMock(EffectivePolicy.class);
        EasyMock.expect(policy.getProvidedEndpointIntents()).andReturn(intents);
        EasyMock.replay(policy);
        return policy;
    }

}

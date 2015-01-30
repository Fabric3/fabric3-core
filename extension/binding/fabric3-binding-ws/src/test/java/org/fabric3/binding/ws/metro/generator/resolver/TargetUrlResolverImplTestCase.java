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

import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;

/**
 *
 */
public class TargetUrlResolverImplTestCase extends TestCase {
    private LogicalBinding<WsBinding> binding;

    public void testSingleVMHttp() throws Exception {
        ServletHost servletHost = EasyMock.createMock(ServletHost.class);
        EasyMock.expect(servletHost.getHttpPort()).andReturn(8080);
        EasyMock.replay(servletHost);

        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM);
        EasyMock.replay(info);

        TargetUrlResolver resolver = new TargetUrlResolverImpl(servletHost, info);

        URL url = resolver.resolveUrl(binding);
        assertEquals("http://localhost:8080/service", url.toString());
        EasyMock.verify(info);
    }

    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
        WsBinding definition = new WsBinding("name", URI.create("service"), null, null, 0);
        LogicalComponent<?> component = new LogicalComponent(null, null, null);
        component.setZone("1");
        LogicalService service = new LogicalService(null, null, component);
        binding = new LogicalBinding<>(definition, service);
    }

}

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
package org.fabric3.contribution.processor;

import java.net.URI;

import f3.BadTestProvider;
import f3.TestProvider;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.ProviderSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class ProviderResourceProcessorTestCase extends TestCase {
    private ProviderResourceProcessor processor;
    private IntrospectionContext context;
    private Resource resource;

    public void testProvider() throws Exception {
        ProviderSymbol symbol = new ProviderSymbol(TestProvider.class.getName());
        ResourceElement<ProviderSymbol, Class<?>> resourceElement = new ResourceElement<ProviderSymbol, Class<?>>(symbol, TestProvider.class);
        resource.addResourceElement(resourceElement);

        processor.index(resource, context);

        assertFalse(context.hasErrors());

        assertEquals(2, resource.getContribution().getResources().size());
    }

    public void testExceptionProvider() throws Exception {
        ProviderSymbol symbol = new ProviderSymbol(BadTestProvider.class.getName());
        ResourceElement<ProviderSymbol, Class<?>> resourceElement = new ResourceElement<ProviderSymbol, Class<?>>(symbol, BadTestProvider.class);
        resource.addResourceElement(resourceElement);

        processor.index(resource, context);

        assertEquals(4, context.getErrors().size());
    }

    public void setUp() throws Exception {
        super.setUp();
        ProcessorRegistry registry = EasyMock.createNiceMock(ProcessorRegistry.class);
        HostInfo info = EasyMock.createNiceMock(HostInfo.class);
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);
        LogicalComponentManager lcm = EasyMock.createMock(LogicalComponentManager.class);
        EasyMock.expect(lcm.getRootComponent()).andReturn(domain);
        EasyMock.replay(lcm);

        processor = new ProviderResourceProcessor(registry, lcm, info);

        Contribution contribution = new Contribution(URI.create("test"));
        resource = new Resource(contribution, null, Constants.DSL_CONTENT_TYPE);
        contribution.addResource(resource);

        ClassLoader classLoader = getClass().getClassLoader();
        context = new DefaultIntrospectionContext(URI.create("test"), classLoader);

    }
}

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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class ProcessorRegistryImplTestCase extends TestCase {
    private ProcessorRegistryImpl registry;
    private IntrospectionContext context;
    private Contribution contribution;
    private Resource resource;

    public void testRegisterUnRegisterContributionProcessor() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.replay(processor);

        registry.register(processor);
        registry.unregister(processor);

        EasyMock.verify(processor);
    }

    public void testRegisterUnRegisterResourceProcessor() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        EasyMock.replay(processor);

        registry.register(processor);

        registry.unregister("application/xml");

        EasyMock.verify(processor);
    }

    public void testProcessManifest() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.processManifest(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processManifest(contribution, context);
        EasyMock.verify(processor);
    }

    public void testIndexContribution() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.index(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.indexContribution(contribution, context);
        EasyMock.verify(processor);
    }

    public void testProcessContribution() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.process(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processContribution(contribution, context);
        EasyMock.verify(processor);
    }

    public void testProcessContributionNoProcessor() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(false);
        EasyMock.replay(processor);

        registry.register(processor);

        try {
            registry.processContribution(contribution, context);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
        EasyMock.verify(processor);

    }

    public void testIndexResource() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        processor.index(EasyMock.isA(Resource.class), EasyMock.eq(context));
        EasyMock.replay(processor);

        registry.register(processor);

        Resource resource = new Resource(contribution, null, "application/xml");
        contribution.addResource(resource);

        registry.indexResource(resource, context);

        EasyMock.verify(processor);
    }

    public void testIndexResourceUnknownType() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("text/plain");
        EasyMock.replay(processor);

        registry.register(processor);

        Resource resource = new Resource(contribution, null, "application/xml");
        contribution.addResource(resource);

        registry.indexResource(resource, context);
        EasyMock.verify(processor);
    }

    public void testProcessResource() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        processor.process(resource, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    public void testProcessResourceUnknownType() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("text/plain");
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    public void testDoNotProcessResourceInError() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        EasyMock.replay(processor);
        resource.setState(ResourceState.ERROR);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new ProcessorRegistryImpl();
        context = new DefaultIntrospectionContext();
        contribution = new Contribution(URI.create("contribution"));
        resource = new Resource(contribution, null, "application/xml");
    }
}

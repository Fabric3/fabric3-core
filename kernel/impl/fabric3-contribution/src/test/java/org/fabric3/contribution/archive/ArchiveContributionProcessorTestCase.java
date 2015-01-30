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
package org.fabric3.contribution.archive;

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.contribution.archive.ArtifactResourceCallback;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class ArchiveContributionProcessorTestCase extends TestCase {
    private ArchiveContributionProcessor processor;

    public void testStartStop() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.register(EasyMock.isA(ContributionProcessor.class));
        registry.unregister(EasyMock.isA(ContributionProcessor.class));
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        processor.start();
        processor.stop();
        EasyMock.verify(registry);
    }

    public void testCanProcess() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(false);
        EasyMock.replay(handler);

        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution1 = new Contribution(URI.create("contribution1"));
        Contribution contribution2 = new Contribution(URI.create("contribution2"));

        assertTrue(processor.canProcess(contribution1));
        assertFalse(processor.canProcess(contribution2));

        EasyMock.verify(handler);
    }

    public void testProcessManifest() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        handler.processManifest(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(handler);

        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution = new Contribution(URI.create("contribution1"));

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        processor.processManifest(contribution, context);

        EasyMock.verify(handler);
    }

    public void testIndex() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        ClassLoader classLoader = getClass().getClassLoader();
        IntrospectionContext context = new DefaultIntrospectionContext(URI.create("test"), classLoader);
        handler.iterateArtifacts(EasyMock.isA(Contribution.class), EasyMock.isA(ArtifactResourceCallback.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Contribution contribution = (Contribution) EasyMock.getCurrentArguments()[0];
                ArtifactResourceCallback callback = (ArtifactResourceCallback) EasyMock.getCurrentArguments()[1];
                Resource resource = new Resource(contribution, new UrlSource(new URL("file://test")), "application/xml");
                callback.onResource(resource);
                return null;
            }
        });

        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.indexResource(EasyMock.isA(Resource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(handler, registry);

        processor.setContributionProcessorRegistry(registry);
        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution = new Contribution(URI.create("contribution1"));

        processor.index(contribution, context);

        EasyMock.verify(handler, registry);
    }

    public void testProcess() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.processResource(EasyMock.isA(Resource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        Contribution contribution = new Contribution(URI.create("contribution1"));
        Resource resource = new Resource(contribution, null, "application/xml");
        contribution.addResource(resource);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(contribution, context);

        EasyMock.verify(registry);
    }

    public void testAlreadyProcessed() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        Contribution contribution = new Contribution(URI.create("contribution1"));
        Resource resource = new Resource(contribution, null, "application/xml");
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(contribution, context);

        EasyMock.verify(registry);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        processor = new ArchiveContributionProcessor();
    }
}

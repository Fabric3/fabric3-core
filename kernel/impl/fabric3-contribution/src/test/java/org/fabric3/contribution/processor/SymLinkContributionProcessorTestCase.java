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
package org.fabric3.contribution.processor;

import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class SymLinkContributionProcessorTestCase extends TestCase {
    private SymLinkContributionProcessor processor;
    private ProcessorRegistry registry;
    private URL file;
    private Contribution contribution;
    private IntrospectionContext context;

    public void testInit() throws Exception {
        registry.register(processor);
        registry.unregister(processor);
        EasyMock.replay(registry);

        processor.init();
        processor.destroy();
        EasyMock.verify(registry);
    }

    public void testCanProcess() throws Exception {
        EasyMock.replay(registry);
        assertTrue(processor.canProcess(contribution));
        EasyMock.verify(registry);
    }

    public void testProcessManifest() throws Exception {
        registry.processManifest(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);
        processor.processManifest(contribution, context);

        assertNotNull(contribution.getMetaData(Contribution.class, contribution.getUri()));
        EasyMock.verify(registry);
    }

    public void testIndex() throws Exception {
        registry.indexContribution(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        Contribution synthetic = new Contribution(URI.create("synthetic"));
        contribution.addMetaData(URI.create("contribution"), synthetic);
        processor.index(contribution, context);
        EasyMock.verify(registry);
    }

    public void testProcess() throws Exception {
        registry.processContribution(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        Contribution synthetic = new Contribution(URI.create("synthetic"));
        contribution.addMetaData(URI.create("contribution"), synthetic);
        processor.process(contribution, context);
        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ProcessorRegistry.class);
        processor = new SymLinkContributionProcessor(registry);
        file = getClass().getResource("sym.contribution");
        contribution = new Contribution(URI.create("contribution"), new UrlSource(file), file, -1, "application/xml", false);
        context = new DefaultIntrospectionContext();
    }
}

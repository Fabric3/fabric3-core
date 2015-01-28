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

import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlContributionProcessorTestCase extends TestCase {
    private XmlContributionProcessor processor;
    private ProcessorRegistry registry;
    private Contribution contribution;
    private IntrospectionContext context;
    private XmlProcessorRegistry xmlProcessorRegistry;
    private XmlIndexerRegistry xmlIndexerRegistry;

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
        EasyMock.replay(registry);
        processor.processManifest(contribution, context);
        EasyMock.verify(registry);
    }

    public void testIndex() throws Exception {
        xmlIndexerRegistry.index(EasyMock.isA(Resource.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(xmlIndexerRegistry);

        processor.index(contribution, context);

        assertFalse(contribution.getResources().isEmpty());
        EasyMock.verify(xmlIndexerRegistry);
    }

    public void testProcess() throws Exception {
        xmlProcessorRegistry.process(EasyMock.isA(Contribution.class),EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(xmlProcessorRegistry);

        processor.process(contribution, context);

        EasyMock.verify(xmlProcessorRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ProcessorRegistry.class);
        xmlProcessorRegistry =    EasyMock.createMock(XmlProcessorRegistry.class);
        xmlIndexerRegistry = EasyMock.createMock(XmlIndexerRegistry.class);
        processor = new XmlContributionProcessor(registry, xmlProcessorRegistry, xmlIndexerRegistry);
        URL file = getClass().getResource("test.composite");
        contribution = new Contribution(URI.create("contribution"), new UrlSource(file), file, -1, "application/xml", false);
        context = new DefaultIntrospectionContext();
    }
}

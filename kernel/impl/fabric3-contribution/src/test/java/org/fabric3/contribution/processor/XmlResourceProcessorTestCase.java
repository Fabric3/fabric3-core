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
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlResourceProcessorTestCase extends TestCase {
    private XmlResourceProcessor processor;
    private ProcessorRegistry registry;
    private IntrospectionContext context;
    private XmlIndexerRegistry xmlIndexerRegistry;
    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private Resource resource;

    public void testInit() throws Exception {
        registry.register(processor);
        registry.unregister("application/xml");
        EasyMock.replay(registry);

        processor.init();
        processor.destroy();
        EasyMock.verify(registry);
    }

    public void testIndex() throws Exception {
        xmlIndexerRegistry.index(EasyMock.isA(Resource.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(xmlIndexerRegistry);

        processor.index(resource, context);

        EasyMock.verify(xmlIndexerRegistry);
    }

    public void testProcess() throws Exception {
        elementLoaderRegistry.load(EasyMock.isA(XMLStreamReader.class), EasyMock.isA(Resource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(elementLoaderRegistry);

        processor.process(resource, context);

        EasyMock.verify(elementLoaderRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(ProcessorRegistry.class);
        elementLoaderRegistry = EasyMock.createMock(XmlResourceElementLoaderRegistry.class);
        xmlIndexerRegistry = EasyMock.createMock(XmlIndexerRegistry.class);
        processor = new XmlResourceProcessor(registry, xmlIndexerRegistry, elementLoaderRegistry);
        URL file = getClass().getResource("test.composite");
        UrlSource source = new UrlSource(file);
        Contribution contribution = new Contribution(URI.create("contribution"), source, file, -1, "application/xml", false);
        resource = new Resource(contribution, source, "application/xml");
        contribution.addResource(resource);
        context = new DefaultIntrospectionContext();
    }
}

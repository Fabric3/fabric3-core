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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlProcessorRegistryImplTestCase extends TestCase {
    private static final QName TYPE = new QName("test", "element");

    public void testProcess() throws Exception {
        XmlProcessor processor = EasyMock.createMock(XmlProcessor.class);
        EasyMock.expect(processor.getType()).andReturn(TYPE);
        processor.processContent(EasyMock.isA(Contribution.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));

        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getName()).andReturn(TYPE);

        EasyMock.replay(processor, reader);

        Contribution contribution = new Contribution(URI.create("test"));

        XmlProcessorRegistryImpl registry = new XmlProcessorRegistryImpl();
        registry.register(processor);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        registry.process(contribution, reader, context);

        registry.unregister(TYPE);

        EasyMock.verify(processor, reader);
    }

}

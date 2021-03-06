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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class XmlIndexerRegistryImplTestCase extends TestCase {
    private static final QName TYPE = new QName("test", "element");

    public void testIndex() throws Exception {
        XmlIndexer indexer = EasyMock.createMock(XmlIndexer.class);
        EasyMock.expect(indexer.getType()).andReturn(TYPE);
        indexer.index(EasyMock.isA(Resource.class), EasyMock.isA(XMLStreamReader.class), EasyMock.isA(IntrospectionContext.class));

        XMLStreamReader reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getName()).andReturn(TYPE);

        EasyMock.replay(indexer, reader);

        Resource resource = new Resource(null, null, null);

        XmlIndexerRegistryImpl registry = new XmlIndexerRegistryImpl();
        registry.register(indexer);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        registry.index(resource, reader, context);

        registry.unregister(TYPE);
        EasyMock.verify(indexer, reader);
    }

}

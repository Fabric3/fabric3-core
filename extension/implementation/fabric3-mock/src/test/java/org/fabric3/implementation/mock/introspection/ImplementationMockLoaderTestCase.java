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
package org.fabric3.implementation.mock.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.implementation.mock.introspection.Bar;
import org.fabric3.implementation.mock.introspection.Baz;
import org.fabric3.implementation.mock.introspection.Foo;
import org.fabric3.implementation.mock.introspection.ImplementationMockLoader;
import org.fabric3.implementation.mock.introspection.MockComponentTypeLoader;
import org.fabric3.implementation.mock.model.ImplementationMock;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class ImplementationMockLoaderTestCase extends TestCase {
    private static final String XML = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:sca='http://docs.oasis-open.org/ns/opencsa/sca/200912'" +
            "           xmlns:f3='urn:fabric3.org'" +
            "           name='org.fabric3.MockImplementation'" +
            "           autowire='true'>" +
            "    <component name='testMock'>" +
            "        <f3:implementation.mock>" +
            "            org.fabric3.implementation.mock.introspection.Foo" +
            "            org.fabric3.implementation.mock.introspection.Bar" +
            "            org.fabric3.implementation.mock.introspection.Baz" +
            "        </f3:implementation.mock>" +
            "    </component>" +
            "" +
            "</composite>";

    public void testLoad() throws Exception {

        MockComponentTypeLoader componentTypeLoader = EasyMock.createMock(MockComponentTypeLoader.class);
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);

        ImplementationMockLoader loader = new ImplementationMockLoader(componentTypeLoader);

        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);

        while (reader.hasNext()) {
            if (reader.next() == XMLStreamConstants.START_ELEMENT && ImplementationMock.IMPLEMENTATION_MOCK.equals(reader.getName())) {
                break;
            }
        }

        ImplementationMock implementationMock = loader.load(reader, context);
        assertNotNull(implementationMock);

        List<String> interfaces = implementationMock.getMockedInterfaces();
        assertEquals(3, interfaces.size());
        assertEquals(Foo.class.getName(), interfaces.get(0));
        assertEquals(Bar.class.getName(), interfaces.get(1));
        assertEquals(Baz.class.getName(), interfaces.get(2));

    }

}

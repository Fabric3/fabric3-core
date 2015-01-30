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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class AbstractValidatingTypeLoaderTestCase extends TestCase {
    private AbstractValidatingTypeLoader<Object> loader;
    private XMLInputFactory factory;
    private DefaultIntrospectionContext context;

    public void testInvalidAttribute() throws Exception {
        String xml = "<composite badAttribute='error'></composite>";
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
        reader.nextTag();

        loader.validateAttributes(reader, context);

        assertFalse(context.getErrors().isEmpty());
        assertTrue(context.getErrors().get(0) instanceof UnrecognizedAttribute);
    }

    public void testValidAttribute() throws Exception {
        String xml = "<composite name='error'></composite>";
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes()));
        reader.nextTag();

        loader.validateAttributes(reader, context);

        assertTrue(context.getErrors().isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();

        loader = new AbstractValidatingTypeLoader<Object>() {

            public Object load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
                return null;
            }
        };

        loader.attributes.add("name");
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
    }
}

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
package org.fabric3.introspection.xml.composite;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.PropertyMany;
import org.fabric3.api.model.type.component.PropertyValue;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 *
 */
public class PropertyValueLoaderTestCase  extends TestCase {
    private static final String SINGLE_VALUE_ATTRIBUTE = "<property name='prop' value='value'/>";
    private static final String INVALID_VALUE = "<property name='prop' value='value'>value</property>";
    private static final String MULTIPLE_VALUES = "<property name='prop' many='true'><value>one</value><value>two</value></property>";
    private static final String INLINE_XML = "<property name='prop'>value</property>";

    private XMLInputFactory factory;
    private PropertyValueLoader loader;
    private IntrospectionContext context;

    public void testLoadInlineValue() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INLINE_XML.getBytes()));
        reader.nextTag();
        PropertyValue value = loader.load(reader,context);
        assertEquals("prop", value.getName());
    }

    public void testLoadAttribute() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(SINGLE_VALUE_ATTRIBUTE.getBytes()));
        reader.nextTag();
        PropertyValue value = loader.load(reader, context);
        assertEquals("prop", value.getName());
        assertFalse(PropertyMany.SINGLE == value.getMany());
        assertEquals("value", value.getValue().getDocumentElement().getFirstChild().getTextContent());
    }

    public void testInvalidValue() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_VALUE.getBytes()));
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof InvalidPropertyValue);
    }

    public void testMultipleValues() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(MULTIPLE_VALUES.getBytes()));
        reader.nextTag();
        PropertyValue value = loader.load(reader, context);
        assertEquals(2, value.getValue().getDocumentElement().getChildNodes().getLength());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        DefaultLoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new PropertyValueLoader(null, loaderHelper);
        context = new DefaultIntrospectionContext();
    }
}

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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.xml.common;

import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

import static org.oasisopen.sca.Constants.SCA_NS;


/**
 *
 */
public class PropertyLoaderTestCase extends TestCase {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private static final String SINGLE_VALUE = "<property name='prop'>value</property>";
    private static final String SINGLE_VALUE_ATTRIBUTE = "<property name='prop' value='value'/>";
    private static final String INVALID_VALUE = "<property name='prop' value='value'>value</property>";
    private static final String MULTIPLE_VALUES = "<property name='prop' many='true'><value>one</value><value>two</value></property>";
    private static final String INVALID_MULTIPLE_VALUES = "<property name='prop'><value>one</value><value>two</value></property>";


    private PropertyLoader loader;
    private XMLInputFactory factory;
    private IntrospectionContext context;

    public void testLoad() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(SINGLE_VALUE.getBytes()));
        reader.nextTag();
        Property property = loader.load(reader, context);
        assertEquals("prop", property.getName());
        assertFalse(property.isMany());
        assertEquals("value", property.getDefaultValue().getDocumentElement().getFirstChild().getTextContent());
    }

    public void testLoadAttribute() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(SINGLE_VALUE_ATTRIBUTE.getBytes()));
        reader.nextTag();
        Property property = loader.load(reader, context);
        assertEquals("prop", property.getName());
        assertFalse(property.isMany());
        assertEquals("value", property.getDefaultValue().getDocumentElement().getFirstChild().getTextContent());
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
        Property property = loader.load(reader, context);
        assertEquals(2, property.getDefaultValue().getDocumentElement().getChildNodes().getLength());
    }

    public void testInvalidMultipleValues() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(INVALID_MULTIPLE_VALUES.getBytes()));
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof InvalidPropertyValue);
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();
        loader = new PropertyLoader(loaderHelper);
    }
}
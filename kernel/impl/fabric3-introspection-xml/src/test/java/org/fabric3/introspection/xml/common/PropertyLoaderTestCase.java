/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.common;

import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.model.type.component.Property;
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
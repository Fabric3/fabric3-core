/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.introspection.xml.composite;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.common.InvalidPropertyValue;
import org.fabric3.api.model.type.component.PropertyMany;
import org.fabric3.api.model.type.component.PropertyValue;
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

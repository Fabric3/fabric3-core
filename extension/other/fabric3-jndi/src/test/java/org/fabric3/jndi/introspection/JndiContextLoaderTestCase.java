/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.jndi.introspection;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.jndi.model.JndiContextDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * @version $Rev$ $Date$
 */
public class JndiContextLoaderTestCase extends TestCase {
    private static final String XML = "<jndi>" +
            "   <context name='Context1'>" +
            "       <property name='name1' value='value1'/>" +
            "       <property name='name2' value='value2'/>" +
            "   </context>" +
            "   <context name='Context2'>" +
            "       <property name='name3' value='value3'/>" +
            "       <property name='name4' value='value4'/>" +
            "   </context>" +
            "</jndi>";

    private static final String MISSING_CONTEXT_NAME_XML = "<jndi><context/></jndi>";

    private static final String MISSING_PROPERTY_NAME_XML = "<jndi>" +
            "   <context name='Context1'>" +
            "       <property value='value1'/>" +
            "   </context>" +
            "</jndi>";

    private static final String MISSING_PROPERTY_VALUE_XML = "<jndi>" +
            "   <context name='Context1'>" +
            "       <property name='name1'/>" +
            "   </context>" +
            "</jndi>";

    public void testLoad() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));

        JndiContextLoader loader = new JndiContextLoader();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        JndiContextDefinition resource = loader.load(reader, context);

        assertFalse(context.hasErrors());
        assertEquals(2, resource.getContexts().size());
        Properties context1 = resource.getContexts().get("Context1");
        assertEquals("value1", context1.getProperty("name1"));
        assertEquals("value2", context1.getProperty("name2"));
        Properties context2 = resource.getContexts().get("Context2");
        assertEquals("value3", context2.getProperty("name3"));
        assertEquals("value4", context2.getProperty("name4"));

    }

    public void testMissingContextName() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(MISSING_CONTEXT_NAME_XML.getBytes()));

        JndiContextLoader loader = new JndiContextLoader();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    public void testMissingPropertyName() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(MISSING_PROPERTY_NAME_XML.getBytes()));

        JndiContextLoader loader = new JndiContextLoader();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    public void testMissingPropertyValue() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(MISSING_PROPERTY_VALUE_XML.getBytes()));

        JndiContextLoader loader = new JndiContextLoader();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

}
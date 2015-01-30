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
package org.fabric3.jndi.introspection;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.api.model.type.resource.jndi.JndiContext;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 *
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
        JndiContext resource = loader.load(reader, context);

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
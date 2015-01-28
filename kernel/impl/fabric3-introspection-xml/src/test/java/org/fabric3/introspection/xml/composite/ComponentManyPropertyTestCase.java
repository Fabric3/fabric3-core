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
package org.fabric3.introspection.xml.composite;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.PropertyMany;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class ComponentManyPropertyTestCase extends TestCase {
    private String XML_NONE = "<component xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='component' "
            + "xmlns:f3='" + org.fabric3.api.Namespaces.F3 + "'>"
            + "<f3:implementation.testing/>"
            + "<property name='prop' many='true'><value>val</value></property>"
            + "</component>";

    private String XML_MANY = "<component xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='component' "
            + "xmlns:f3='" + org.fabric3.api.Namespaces.F3 + "'>"
            + "<f3:implementation.testing/>"
            + "<property name='prop' many='true'><value>val</value></property>"
            + "</component>";

    private String XML_SINGLE = "<component xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='component' "
            + "xmlns:f3='" + org.fabric3.api.Namespaces.F3 + "'>"
            + "<f3:implementation.testing/>"
            + "<property name='prop' many='false'><value>val</value></property>"
            + "</component>";

    private ComponentLoader loader;
    private IntrospectionContext context;
    private MockImplementationLoader implLoader;

    public void testManyInheritence() throws Exception {
        Property property = new Property("prop");
        property.setMany(true);
        implLoader.setProperties(property);

        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_NONE.getBytes()));
        reader.nextTag();
        ComponentDefinition<?> definition = loader.load(reader, context);
        assertEquals(PropertyMany.MANY, definition.getPropertyValues().get("prop").getMany());
    }

    public void testNarrowMany() throws Exception {
        Property property = new Property("prop");
        property.setMany(true);
        implLoader.setProperties(property);

        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_SINGLE.getBytes()));
        reader.nextTag();
        ComponentDefinition<?> definition = loader.load(reader, context);
        assertEquals(PropertyMany.SINGLE, definition.getPropertyValues().get("prop").getMany());
    }

    public void testWidenMany() throws Exception {
        Property property = new Property("prop");
        property.setMany(false);
        implLoader.setProperties(property);

        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML_MANY.getBytes()));
        reader.nextTag();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof InvalidPropertyConfiguration);
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl();
        LoaderHelper helper = new DefaultLoaderHelper();
        PropertyValueLoader pvLoader = new PropertyValueLoader(registry, helper);
        pvLoader.init();

        implLoader = new MockImplementationLoader();
        registry.registerLoader(MockImplementation.TYPE, implLoader);
        loader = new ComponentLoader(registry, helper);

        context = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }

}
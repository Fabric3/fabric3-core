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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.introspection.xml.common.ComponentReferenceLoader;
import org.fabric3.api.model.type.Comment;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.Text;
import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.xml.XMLFactory;

/**
 *
 */
public class ComponentLoaderRoundTripTestCase extends TestCase {
    private String XML = "<component xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='component' "
            + "xmlns:f3='" + org.fabric3.api.Namespaces.F3 + "'>"
            + "<f3:implementation.testing/>"
            + "<!-- comment -->\n"
            + "<reference name='ref1' target='target'/>"
            + "<reference name='ref2' target='target'/>"
            + "</component>";

    private ComponentLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    public void testRoundTrip() throws Exception {
        loader.setRoundTrip(true);
        ComponentDefinition<?> definition = loader.load(reader, ctx);
        List<ModelObject> stack = definition.getElementStack();
        assertFalse(ctx.hasErrors());
        // verify order of reads
        assertTrue(stack.get(0) instanceof Implementation);
        assertTrue(stack.get(1) instanceof Comment);
        assertTrue(stack.get(2) instanceof Text);
        assertEquals("ref1", ((AbstractReference) stack.get(3)).getName());
        assertEquals("ref2", ((AbstractReference) stack.get(4)).getName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl(new MockXMLFactory());
        LoaderHelper helper = new DefaultLoaderHelper();
        ComponentReferenceLoader referenceLoader = new ComponentReferenceLoader(registry, helper);
        referenceLoader.init();

        MockImplementationLoader implLoader = new MockImplementationLoader();
        implLoader.setReferences(new ReferenceDefinition("ref1", Multiplicity.ONE_ONE), new ReferenceDefinition("ref2", Multiplicity.ONE_ONE));
        registry.registerLoader(MockImplementation.TYPE, implLoader);
        loader = new ComponentLoader(registry, helper);

        XMLFactory factory = new MockXMLFactory();
        reader = factory.newInputFactoryInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }


}
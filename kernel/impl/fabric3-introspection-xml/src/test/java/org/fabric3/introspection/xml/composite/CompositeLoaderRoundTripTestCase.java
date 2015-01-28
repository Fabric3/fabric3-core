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
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.Comment;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.Namespace;
import org.fabric3.api.model.type.Text;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class CompositeLoaderRoundTripTestCase extends TestCase {
    private String XML = "<composite xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' targetNamespace='urn:test' name='component' "
            + "xmlns:f3='" + org.fabric3.api.Namespaces.F3 + "'>"
            + "<!-- comment -->\n"
            + "</composite>";

    private CompositeLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    public void testRoundTrip() throws Exception {
        loader.setRoundTrip(true);
        Composite composite = loader.load(reader, ctx);
        List<ModelObject> stack = composite.getElementStack();
        assertFalse(ctx.hasErrors());
        assertEquals(2, composite.getNamespaces().size());
        for (Namespace namespace : composite.getNamespaces()) {
            assertTrue(namespace.getPrefix() == null || namespace.getPrefix().length() == 0 || "f3".equals(namespace.getPrefix()));
        }
        // verify order of reads
        assertTrue(stack.get(0) instanceof Comment);
        assertTrue(stack.get(1) instanceof Text);
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl();
        final DefaultLoaderHelper helper = new DefaultLoaderHelper();

        loader = new CompositeLoader(registry, null, helper);

        reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }


}
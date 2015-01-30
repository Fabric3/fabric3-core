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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.common.ComponentServiceLoader;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class ComponentServiceLoaderTestCase extends TestCase {
    private static final QName IMPLEMENTATION_MOCK = new QName(org.fabric3.api.Namespaces.F3, "implementation.mock");
    private static final String XML = "<service xmlns='http://docs.oasis-open.org/ns/opencsa/sca/200912' name='service'/>";

    private ComponentServiceLoader loader;
    private XMLStreamReader reader;
    private IntrospectionContext ctx;

    public void testLoad() throws Exception {
        ServiceDefinition service = loader.load(reader, ctx);
        assertEquals("service", service.getName());
        assertFalse(ctx.hasErrors());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderRegistry registry = new LoaderRegistryImpl();

        MockImplementationLoader implLoader = new MockImplementationLoader();
        registry.registerLoader(IMPLEMENTATION_MOCK, implLoader);
        loader = new ComponentServiceLoader(registry);

        reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        ctx = new DefaultIntrospectionContext(URI.create("parent"), getClass().getClassLoader(), null, "foo");
    }

}
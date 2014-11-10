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

package org.fabric3.introspection.xml.binding;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.binding.SCABinding;

/**
 *
 */
public class SCABindingLoaderTestCase extends TestCase {
    private static final String XML = "<binding.sca name='name' uri='Component/Service/Binding'/>";
    private SCABindingLoader loader;
    private XMLInputFactory factory;

    public void testLoader() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        SCABinding binding = loader.load(reader, context);
        assertEquals("name", binding.getName());
        assertEquals("Component", binding.getTarget().getComponent());
        assertEquals("Service", binding.getTarget().getBindable());
        assertEquals("Binding", binding.getTarget().getBinding());
        assertTrue(context.getErrors().isEmpty());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();
        loader = new SCABindingLoader(null, new DefaultLoaderHelper());
    }
}
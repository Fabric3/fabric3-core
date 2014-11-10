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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.manifest;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 *
 */
public class ExtendsLoaderTestCase extends TestCase {

    private static final String XML = "<extends name='some-extension'/>";

    private ExtendsLoader loader;
    private XMLStreamReader reader;

    public void testLoad() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        ExtendsDeclaration declaration = loader.load(reader, context);
        assertFalse(context.hasErrors());
        assertEquals("some-extension", declaration.getName());
    }

    protected void setUp() throws Exception {
        loader = new ExtendsLoader();
        ByteArrayInputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }


}

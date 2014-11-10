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
package org.fabric3.binding.file.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBindingDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;

public class FileBindingLoaderTestCase extends TestCase {
    private static final String REFERENCE_BINDING_CONFIG =
            "<binding.file name='file' pattern='trans***' location='/dir/subdir' error.location='/dir/error' delay='333'/>";
    private static final String STRATEGY_BINDING_CONFIG =
            "<binding.file name='file' location='/dir/subdir' strategy='archive' archive.location='/dir/output' error.location='/dir/error'/>";

    private static final String NO_ARCHIVE_BINDING_CONFIG = "<binding.file name='file' location='/dir/subdir' strategy='archive'/>";

    private XMLInputFactory xmlFactory;
    private FileBindingLoader loader;

    public void testLoadBindingElement() throws Exception {
        XMLStreamReader reader = createReader(REFERENCE_BINDING_CONFIG);
        IntrospectionContext context = new DefaultIntrospectionContext();
        FileBindingDefinition definition = loader.load(reader, context);
        assertFalse(context.hasErrors());

        assertEquals("file", definition.getName());
        assertEquals("trans***", definition.getPattern());
        assertEquals("/dir/subdir", definition.getLocation());
        assertEquals("/dir/error", definition.getErrorLocation());
        assertEquals(333, definition.getDelay());
    }

    public void testLoadArchiveStrategy() throws Exception {
        XMLStreamReader reader = createReader(STRATEGY_BINDING_CONFIG);
        IntrospectionContext context = new DefaultIntrospectionContext();
        FileBindingDefinition definition = loader.load(reader, context);
        assertFalse(context.hasErrors());

        assertEquals("file", definition.getName());
        assertEquals("/dir/subdir", definition.getLocation());
        assertEquals(Strategy.ARCHIVE, definition.getStrategy());
        assertEquals("/dir/output", definition.getArchiveLocation());
    }

    public void testLoadNoArchive() throws Exception {
        XMLStreamReader reader = createReader(NO_ARCHIVE_BINDING_CONFIG);
        IntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, context);
        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new FileBindingLoader(helper);
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;
    }

}

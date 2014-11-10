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
package org.fabric3.monitor.appender.component;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class ComponentAppenderLoaderTestCase extends TestCase {
    private static final String XML = "<appender.component name='test'/>";
    private static final String XML_NO_NAME = "<appender.component />";

    private LoaderRegistry registry;
    private ComponentAppenderLoader loader;

    public void testLoad() throws Exception {
        EasyMock.replay(registry);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();
        ComponentAppenderDefinition definition = loader.load(reader, context);

        assertEquals("test", definition.getComponentName());
        assertFalse(context.hasErrors());
        EasyMock.verify(registry);
    }

    public void testLoadNoName() throws Exception {
        EasyMock.replay(registry);
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();
        ComponentAppenderDefinition definition = loader.load(reader, context);

        assertEquals("", definition.getComponentName());
        assertTrue(context.hasErrors());
        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(LoaderRegistry.class);
        loader = new ComponentAppenderLoader(registry);

    }
}

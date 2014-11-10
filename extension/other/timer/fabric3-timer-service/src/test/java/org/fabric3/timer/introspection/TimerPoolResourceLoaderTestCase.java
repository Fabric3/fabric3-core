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
package org.fabric3.timer.introspection;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.api.model.type.resource.timer.TimerPoolResource;

/**
 *
 */
public class TimerPoolResourceLoaderTestCase extends TestCase {

    private static final String XML = "<timer.pool name='test' size='5'/>";
    private static final String XML_MISSING_NAME = "<timer.pool size='5'/>";
    private static final String XML_DEFAULT_SIZE = "<timer.pool name='test'/>";

    private TimerPoolResourceLoader loader = new TimerPoolResourceLoader();

    public void testParse() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.next();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        TimerPoolResource resource = loader.load(reader, context);
        assertFalse(context.hasErrors());
        assertEquals("test", resource.getName());
        assertEquals(5, resource.getCoreSize());
    }

    public void testDefaultSize() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(XML_DEFAULT_SIZE.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.next();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        TimerPoolResource resource = loader.load(reader, context);
        assertFalse(context.hasErrors());
        assertEquals("test", resource.getName());
        assertTrue(resource.getCoreSize() > 0);
    }

    public void testParseMissingName() throws Exception {
        ByteArrayInputStream stream = new ByteArrayInputStream(XML_MISSING_NAME.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.next();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        loader.load(reader, context);
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

}
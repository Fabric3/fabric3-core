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
package org.fabric3.monitor.appender.file;

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
public class FileAppenderLoaderTestCase extends TestCase {
    private static final String NO_STRATEGY = "<appender.file file='test'/>";
    private static final String SIZE_STRATEGY = "<appender.file file='test' roll.type='size' roll.size='10'/>";
    private static final String NO_FILE = "<appender.file/>";
    private static final String INVALID_SIZE = "<appender.file file='test' roll.type='size' roll.size='10e'/>";
    private static final String INVALID_STRATEGY = "<appender.file file='test' roll.type='error'/>";

    private LoaderRegistry loaderRegistry;
    private LoaderMonitor monitor;
    private FileAppenderLoader loader;

    public void testCreateFileAppenderNoRoll() throws Exception {
        EasyMock.replay(loaderRegistry, monitor);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(NO_STRATEGY.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();

        FileAppenderDefinition definition = loader.load(reader, context);

        assertFalse(context.hasErrors());
        assertEquals("none", definition.getRollType());

        EasyMock.verify(loaderRegistry, monitor);
    }

    public void testCreateFileAppenderRollSize() throws Exception {
        EasyMock.replay(loaderRegistry, monitor);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(SIZE_STRATEGY.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();

        FileAppenderDefinition definition = loader.load(reader, context);

        assertFalse(context.hasErrors());
        assertEquals("size", definition.getRollType());
        assertEquals(10, definition.getRollSize());

        EasyMock.verify(loaderRegistry, monitor);
    }

    public void testCreateFileAppenderNoFile() throws Exception {
        EasyMock.replay(loaderRegistry, monitor);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(NO_FILE.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();

        loader.load(reader, context);

        assertTrue(context.hasErrors());
        EasyMock.verify(loaderRegistry, monitor);
    }

    public void testInvalidSize() throws Exception {
        monitor.invalidRollSize("test", "10e");
        EasyMock.replay(loaderRegistry, monitor);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(INVALID_SIZE.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();

        loader.load(reader, context);

        EasyMock.verify(loaderRegistry, monitor);
    }

    public void testInvalidRollStrategy() throws Exception {
        monitor.invalidRollType("test", "error");
        EasyMock.replay(loaderRegistry, monitor);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(new ByteArrayInputStream(INVALID_STRATEGY.getBytes()));
        reader.nextTag();

        IntrospectionContext context = new DefaultIntrospectionContext();

        loader.load(reader, context);

        EasyMock.verify(loaderRegistry, monitor);
    }

    public void setUp() throws Exception {
        super.setUp();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        monitor = EasyMock.createMock(LoaderMonitor.class);

        loader = new FileAppenderLoader(loaderRegistry, monitor);
    }

}

/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.impl.appender.file;

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

        FileAppenderDefinition definition = loader.load(reader, context);

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

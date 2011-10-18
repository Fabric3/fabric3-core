/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.file.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.binding.file.common.Strategy;
import org.fabric3.binding.file.model.FileBindingDefinition;
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

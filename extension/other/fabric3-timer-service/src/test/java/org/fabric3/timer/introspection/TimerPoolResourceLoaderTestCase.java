/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.timer.introspection;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.timer.model.TimerPoolResource;

/**
 * @version $Rev$ $Date$
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
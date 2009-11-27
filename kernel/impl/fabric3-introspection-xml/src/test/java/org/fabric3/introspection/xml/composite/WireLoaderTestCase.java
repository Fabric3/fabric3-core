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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.composite;

import java.net.URI;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * @version $Rev: 7275 $ $Date: 2009-07-05 21:54:59 +0200 (Sun, 05 Jul 2009) $
 */
public class WireLoaderTestCase extends TestCase {
    private WireLoader wireLoader;
    private XMLStreamReader reader;

    public void testWithNoServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source"), definition.getSource());
        assertEquals(URI.create("target"), definition.getTarget());
    }

    public void testWithServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source/s");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target/t");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source#s"), definition.getSource());
        assertEquals(URI.create("target#t"), definition.getTarget());
    }

    protected void setUp() throws Exception {
        super.setUp();
        LoaderHelper helper = new DefaultLoaderHelper();
        wireLoader = new WireLoader(helper);
        reader = EasyMock.createStrictMock(XMLStreamReader.class);
    }
}

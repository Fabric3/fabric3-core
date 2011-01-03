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
package org.fabric3.monitor.introspection;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.monitor.model.MonitorImplementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * @version $Rev: 8696 $ $Date: 2010-03-12 15:47:05 +0100 (Fri, 12 Mar 2010) $
 */
@EagerInit
public class MonitorImplementationLoaderTestCase extends TestCase {
    private static DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private static final String XML_CONFIGURATION = "<implementation.monitor><configuration></configuration></implementation.monitor>";
    private static final String XML_NO_CONFIGURATION = "<implementation.monitor/>";
    Document configuration;

    public void testLoadConfiguration() throws Exception {
        LoaderHelper helper = EasyMock.createMock(LoaderHelper.class);
        EasyMock.expect(helper.transform(EasyMock.isA(XMLStreamReader.class))).andReturn(configuration);
        EasyMock.replay(helper);
        MonitorImplementationLoader loader = new MonitorImplementationLoader(helper);
        ByteArrayInputStream stream = new ByteArrayInputStream(XML_CONFIGURATION.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        MonitorImplementation implementation = loader.load(reader, context);
        assertEquals("configuration", implementation.getConfiguration().getNodeName());
        EasyMock.verify(helper);
    }

    public void testLoadNoConfiguration() throws Exception {
        LoaderHelper helper = EasyMock.createMock(LoaderHelper.class);
        EasyMock.replay(helper);
        MonitorImplementationLoader loader = new MonitorImplementationLoader(helper);
        ByteArrayInputStream stream = new ByteArrayInputStream(XML_NO_CONFIGURATION.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        MonitorImplementation implementation = loader.load(reader, context);
        assertNull(implementation.getConfiguration());
        EasyMock.verify(helper);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = FACTORY.newDocumentBuilder().newDocument();
        Element element = configuration.createElement("configuration");
        configuration.appendChild(element);
    }
}
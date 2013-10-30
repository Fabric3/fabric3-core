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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.zeromq.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

public class ZeroMQBindingLoaderTestCase extends TestCase {
    private static final String BINDING_CONFIG =
            "<binding.zeromq name='zmq' addresses='localhost:8080 localhost:8181' high.water='1' multicast.rate='2' multicast.recovery='3' send.buffer='4' receive.buffer='5'/>";

    private XMLInputFactory xmlFactory;
    private ZeroMQBindingLoader loader;

    public void testLoadZeroMQBindingElement() throws Exception {
        XMLStreamReader reader = createReader(BINDING_CONFIG);
        IntrospectionContext context = new DefaultIntrospectionContext();
        ZeroMQBindingDefinition definition = loader.load(reader, context);
        assertFalse(context.hasErrors());

        assertEquals("zmq", definition.getName());
        ZeroMQMetadata metadata = definition.getZeroMQMetadata();
        List<SocketAddressDefinition> addresses = metadata.getSocketAddresses();
        assertEquals(2, addresses.size());
        assertEquals(8080, addresses.get(0).getPort());
        assertEquals("localhost", addresses.get(0).getHost());
        assertEquals(8181, addresses.get(1).getPort());
        assertEquals("localhost", addresses.get(1).getHost());

        assertEquals(1, metadata.getHighWater());
        assertEquals(2, metadata.getMulticastRate());
        assertEquals(3, metadata.getMulticastRecovery());
        assertEquals(4, metadata.getSendBuffer());
        assertEquals(5, metadata.getReceiveBuffer());


    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new ZeroMQBindingLoader(helper);
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;
    }

}

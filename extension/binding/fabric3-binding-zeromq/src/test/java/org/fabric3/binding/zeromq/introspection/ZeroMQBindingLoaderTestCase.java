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
package org.fabric3.binding.zeromq.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

public class ZeroMQBindingLoaderTestCase extends TestCase {
    private static final String BINDING_CONFIG =
            "<binding.zeromq name='zmq' addresses='localhost:8080 localhost:8181' high.water='1' multicast.rate='2' multicast.recovery='3' send.buffer='4' receive.buffer='5'/>";

    private XMLInputFactory xmlFactory;
    private ZeroMQBindingLoader loader;

    public void testLoadZeroMQBindingElement() throws Exception {
        XMLStreamReader reader = createReader(BINDING_CONFIG);
        IntrospectionContext context = new DefaultIntrospectionContext();
        ZeroMQBinding definition = loader.load(reader, context);
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
        loader = new ZeroMQBindingLoader();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;
    }

}

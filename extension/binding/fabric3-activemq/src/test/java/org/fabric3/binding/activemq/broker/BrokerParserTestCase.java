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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.activemq.broker;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 *
 */
public class BrokerParserTestCase extends TestCase {
    private static final String XML = "<foo><value><broker name='brokerName'>" +
            "    <networkConnectors>" +
            "        <networkConnector uri='multicast://default'/>" +
            "    </networkConnectors>" +
            "    <persistenceAdapter type='amq' syncOnWrite='false' maxFileLength='20 mb'/>" +
            "    <transportConnectors>" +
            "        <transportConnector name='openwire' uri='tcp://localhost:61616' discoveryUri='multicast://default'/>" +
            "        <transportConnector name='ssl' uri='ssl://localhost:61617'/>" +
            "        <transportConnector name='stomp' uri='stomp://localhost:61613'/>" +
            "        <transportConnector name='xmpp' uri='xmpp://localhost:61222'/>" +
            "    </transportConnectors>" +
            "</broker></value></foo>";

    private static final String NO_BROKER_NAME = "<foo><value><broker>\n" +
            "                               <networkConnectors>\n" +
            "                                   <networkConnector uri='multicast://default'/>\n" +
            "                               </networkConnectors>\n" +
            "                               <transportConnectors>\n" +
            "                                   <transportConnector name='openwire' uri='tcp://localhost:61616'/>\n" +
            "                               </transportConnectors>\n" +
            "                           </broker></value></foo>";
    private BrokerParser parser = new BrokerParser();

    public void testParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        BrokerConfiguration configuration = parser.parse(reader);
        assertEquals(1, configuration.getNetworkConnectorUris().size());
        assertEquals("multicast://default", configuration.getNetworkConnectorUris().get(0).toString());
        TransportConnectorConfig connectorConfig = configuration.getTransportConnectorConfigs().get(0);
        assertEquals("tcp://localhost:61616", connectorConfig.getUri().toString());
        assertEquals("multicast://default", connectorConfig.getDiscoveryUri().toString());
        assertEquals(4, configuration.getTransportConnectorConfigs().size());
    }

    public void testNoBrokerName() throws Exception {
        InputStream stream = new ByteArrayInputStream(NO_BROKER_NAME.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        parser.parse(reader);
    }

}

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
package org.fabric3.binding.activemq.broker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.binding.activemq.factory.InvalidConfigurationException;

/**
 * @version $Rev$ $Date$
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
        try {
            BrokerConfiguration configuration = parser.parse(reader);
            fail();
        } catch (InvalidConfigurationException e) {
            // expected
        }
    }

}

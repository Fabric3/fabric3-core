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
package org.fabric3.binding.activemq.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.easymock.EasyMock;

import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.FactoryRegistrationException;
import org.fabric3.host.runtime.HostInfo;

/**
 * @version $Rev$ $Date$
 */
public class ConnectionFactoryParserTestCase extends TestCase {
    private static final String XML = "<foo><value>" +
            "<connection.factories>" +
            "   <connection.factory name='testFactory' broker.url='vm://broker' type='xa'>" +
            "      <factory.properties>" +
            "           <optimizedMessageDispatch>true</optimizedMessageDispatch>" +
            "      </factory.properties>" +
            "      <pool.properties>" +
            "            <maxSize>10</maxSize>" +
            "      </pool.properties>" +
            "   </connection.factory>" +
            "   <connection.factory name='nonXAtestFactory' broker.url='vm://broker'/>" +
            "</connection.factories>" +
            "</value></foo>";

    private ConnectionFactoryParser parser;
    private XMLStreamReader reader;
    private MockConnectionFactoryManager registry;


    public void testParse() throws Exception {
        parser.setConnectionFactories(reader);
        parser.init();
        registry.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new MockConnectionFactoryManager();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeId()).andReturn("broker");
        EasyMock.replay(info);
        parser = new ConnectionFactoryParser(registry, info);

        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();


    }

    private class MockConnectionFactoryManager implements ConnectionFactoryManager {
        private Map<String, ConnectionFactory> factories = new HashMap<String, ConnectionFactory>();

        public ConnectionFactory get(String name) {
            return factories.get(name);
        }

        public ConnectionFactory register(String name, ConnectionFactory factory) throws FactoryRegistrationException {
            return null;
        }

        public ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) {
            factories.put(name, factory);
            if ("testFactory".equals(name)) {
                assertNotNull(properties.get("maxSize"));
            }
            return factory;
        }

        public void unregister(String name) {
            factories.remove(name);
        }

        public void verify() {
            assertEquals(2, factories.size());
            ActiveMQXAConnectionFactory xaFactory = (ActiveMQXAConnectionFactory) factories.get("testFactory");
            assertEquals("vm://broker", xaFactory.getBrokerURL());
            ActiveMQConnectionFactory nonXaFactory = (ActiveMQConnectionFactory) factories.get("nonXAtestFactory");
            assertEquals("vm://broker", nonXaFactory.getBrokerURL());
        }
    }
}

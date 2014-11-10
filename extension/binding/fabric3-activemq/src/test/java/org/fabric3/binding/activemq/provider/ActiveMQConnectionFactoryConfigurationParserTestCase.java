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
package org.fabric3.binding.activemq.provider;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 *
 */
public class ActiveMQConnectionFactoryConfigurationParserTestCase extends TestCase {
    private static final String XML = "   <connection.factory name='testFactory' broker.url='vm://broker' type='xa' username='foo' password='bar'>" +
                                      "      <factory.properties>" +
                                      "           <optimizedMessageDispatch>true</optimizedMessageDispatch>" +
                                      "      </factory.properties>" +
                                      "   </connection.factory>";

    private ActiveMQConnectionFactoryConfigurationParser parser;
    private XMLStreamReader reader;

    public void testParse() throws Exception {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        ConnectionFactoryConfiguration configuration = parser.parse(reader, context);
        assertEquals("vm://broker", configuration.getAttribute(URI.class, "broker.uri").toString());
        assertEquals("testFactory", (configuration.getName()));
        assertEquals(1, configuration.getFactoryProperties().size());
        assertEquals("foo", configuration.getUsername());
        assertEquals("bar", configuration.getPassword());
    }

    protected void setUp() throws Exception {
        super.setUp();
        BrokerHelper helper = EasyMock.createMock(BrokerHelper.class);
        EasyMock.expect(helper.getDefaultBrokerName()).andReturn("broker");
        EasyMock.replay(helper);
        parser = new ActiveMQConnectionFactoryConfigurationParser(helper);

        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}

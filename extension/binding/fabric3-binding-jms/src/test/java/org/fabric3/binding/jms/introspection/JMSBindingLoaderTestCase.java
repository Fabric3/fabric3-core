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
package org.fabric3.binding.jms.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.jms.model.ActivationSpec;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.HeadersDefinition;
import org.fabric3.api.binding.jms.model.JmsBinding;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.OperationPropertiesDefinition;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

public class JMSBindingLoaderTestCase extends TestCase {
    // wireFormat; activation spec; messageSelection
    private static final String JMS_BINDING = "   <binding.jms correlationScheme='correlationID'>" +
                                              "      <destination jndiName='serviceQueue' type='queue' create='always'>" +
                                              "         <property name='prop1'>val</property>" +
                                              "      </destination>" +
                                              "      <connectionFactory jndiName='serviceQueue' create='always'>" +
                                              "         <property name='prop1'>val</property>" +
                                              "      </connectionFactory>" +
                                              "      <response>" +
                                              "          <destination jndiName='clientQueue' type='queue' create='always'/>" +
                                              "          <connectionFactory jndiName='clientQueue' create='always'>" +
                                              "             <property name='prop1'>val</property>" +
                                              "          </connectionFactory>" +
                                              "       </response>" +
                                              "       <headers type='jmstype' deliveryMode='persistent' timeToLive='10000' priority='1'>" +
                                              "          <property name='headerProp'>val</property>" +
                                              "       </headers>" +
                                              "       <operationProperties name='testOperationProperties1'>" +
                                              "          <property name='testHeadersPropertyProperty'>TestHeadersPropertyProperty</property>" +
                                              "          <headers>" +
                                              "             <property name='nested'>NestedHeader</property>" +
                                              "          </headers>" +
                                              "       </operationProperties>" +
                                              "       <operationProperties name='testOperationProperties2' selectedOperation='NativeName'/>" +
                                              "   </binding.jms>";

    private static final String ACTIVATION_SPEC = "   <binding.jms>" +
                                                  "<activationSpec jndiName='serviceQueue' create='always'>" +
                                                  "   <property name='prop1'>val</property>" +
                                                  "</activationSpec>" +
                                                  "   <response>" +
                                                  "      <activationSpec jndiName='clientQueue' create='always'>" +
                                                  "         <property name='prop1'>val</property>" +
                                                  "      </activationSpec>" +
                                                  "   </response>" +
                                                  "</binding.jms>";

    private static final String MESSAGE_SELECTION = "   <binding.jms>" +
                                                    "   <messageSelection selector='select'>" +
                                                    "      <property name='prop1'>val</property>" +
                                                    "   </messageSelection>" +
                                                    "</binding.jms>";

    private static final String BINDING_HANDLER = "   <binding.jms>" +
                                                  "   <destination jndiName='serviceQueue' type='queue' create='always'>" +
                                                  "         <property name='prop1'>val</property> " +
                                                  "      </destination>" +
                                                  "         <f3:handler target=\"SomeHandler\" xmlns:f3=\"urn:fabric3.org\" />" +
                                                  "</binding.jms>";

    private XMLInputFactory factory;
    private JmsBindingLoader loader;
    private IntrospectionContext context;
    private BindingHandlerRegistry handlerRegistry;

    public void testGeneralParse() throws Exception {
        XMLStreamReader streamReader = factory.createXMLStreamReader(new ByteArrayInputStream(JMS_BINDING.getBytes()));
        streamReader.nextTag();

        JmsBinding binding = loader.load(streamReader, context);
        JmsBindingMetadata metadata = binding.getJmsMetadata();

        // verify destination configuration
        assertEquals(CorrelationScheme.CORRELATION_ID, metadata.getCorrelationScheme());
        Destination destination = metadata.getDestination();
        assertEquals("serviceQueue", destination.getName());
        assertEquals(DestinationType.QUEUE, destination.geType());
        assertEquals(CreateOption.ALWAYS, destination.getCreate());
        assertEquals(1, destination.getProperties().size());
        assertEquals("val", destination.getProperties().get("prop1"));

        // verify connection factory
        ConnectionFactoryDefinition connectionFactory = metadata.getConnectionFactory();
        assertEquals("serviceQueue", connectionFactory.getName());
        assertEquals(CreateOption.ALWAYS, connectionFactory.getCreate());
        assertEquals("val", connectionFactory.getProperties().get("prop1"));

        // verify response
        Destination responseDestination = metadata.getResponseDestination();
        assertEquals("clientQueue", responseDestination.getName());
        assertEquals(DestinationType.QUEUE, responseDestination.geType());
        assertEquals(CreateOption.ALWAYS, responseDestination.getCreate());
        ConnectionFactoryDefinition responseConnectionFactory = metadata.getResponse().getConnectionFactory();
        assertEquals("clientQueue", responseConnectionFactory.getName());
        assertEquals(CreateOption.ALWAYS, responseConnectionFactory.getCreate());
        assertEquals("val", responseConnectionFactory.getProperties().get("prop1"));

        // verify headers
        HeadersDefinition headers = metadata.getHeaders();
        assertEquals("jmstype", headers.getJmsType());
        assertEquals(DeliveryMode.PERSISTENT, headers.getDeliveryMode());
        assertEquals(10000, headers.getTimeToLive());
        assertEquals(1, headers.getPriority());
        assertEquals("val", headers.getProperties().get("headerProp"));

        // verify operation properties
        Map<String, OperationPropertiesDefinition> properties = metadata.getOperationProperties();
        assertEquals(2, properties.size());
        assertEquals("TestHeadersPropertyProperty", properties.get("testOperationProperties1").getProperties().get("testHeadersPropertyProperty"));
        assertEquals("NestedHeader", properties.get("testOperationProperties1").getHeaders().getProperties().get("nested"));
        assertEquals("NativeName", properties.get("testOperationProperties2").getSelectedOperation());
    }

    public void testActivationParse() throws Exception {
        XMLStreamReader streamReader = factory.createXMLStreamReader(new ByteArrayInputStream(ACTIVATION_SPEC.getBytes()));
        streamReader.nextTag();

        JmsBinding binding = loader.load(streamReader, context);
        JmsBindingMetadata metadata = binding.getJmsMetadata();
        ActivationSpec spec = metadata.getActivationSpec();
        assertEquals("serviceQueue", spec.getName());
        assertEquals(CreateOption.ALWAYS, spec.getCreate());
        assertEquals("val", spec.getProperties().get("prop1"));

        ActivationSpec responseSpec = metadata.getResponse().getActivationSpec();
        assertEquals("clientQueue", responseSpec.getName());
        assertEquals(CreateOption.ALWAYS, responseSpec.getCreate());
        assertEquals("val", responseSpec.getProperties().get("prop1"));
    }

    public void testMessageSelectionParse() throws Exception {
        XMLStreamReader streamReader = factory.createXMLStreamReader(new ByteArrayInputStream(MESSAGE_SELECTION.getBytes()));
        streamReader.nextTag();

        JmsBinding binding = loader.load(streamReader, context);
        JmsBindingMetadata metadata = binding.getJmsMetadata();
        MessageSelection messageSelection = metadata.getMessageSelection();
        assertEquals("select", messageSelection.getSelector());
        assertEquals("val", messageSelection.getProperties().get("prop1"));

    }

    public void testBindingHandler() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(BINDING_HANDLER.getBytes()));
        reader.nextTag();

        LoaderRegistry registry = EasyMock.createStrictMock(LoaderRegistry.class);

        URI uri = URI.create("TestHandler");
        EasyMock.expect(registry.load(reader, BindingHandler.class, context)).andReturn(new BindingHandler(uri));
        EasyMock.replay(handlerRegistry, registry);

        JmsBindingLoader loader = new JmsBindingLoader(registry);

        loader.load(reader, context);

        EasyMock.verify(handlerRegistry, registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();

        LoaderRegistry registry = EasyMock.createNiceMock(LoaderRegistry.class);
        handlerRegistry = EasyMock.createNiceMock(BindingHandlerRegistry.class);
        EasyMock.replay(registry);

        loader = new JmsBindingLoader(registry);
        context = new DefaultIntrospectionContext();
    }
}

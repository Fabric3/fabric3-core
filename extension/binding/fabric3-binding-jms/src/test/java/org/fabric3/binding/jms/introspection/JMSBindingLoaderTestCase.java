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
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.HeadersDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.binding.jms.model.MessageSelection;
import org.fabric3.api.binding.jms.model.OperationPropertiesDefinition;
import org.fabric3.api.model.type.component.BindingHandlerDefinition;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
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

        JmsBindingDefinition binding = loader.load(streamReader, context);
        JmsBindingMetadata metadata = binding.getJmsMetadata();

        // verify destination configuration
        assertEquals(CorrelationScheme.CORRELATION_ID, metadata.getCorrelationScheme());
        DestinationDefinition destination = metadata.getDestination();
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
        DestinationDefinition responseDestination = metadata.getResponseDestination();
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

        JmsBindingDefinition binding = loader.load(streamReader, context);
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

        JmsBindingDefinition binding = loader.load(streamReader, context);
        JmsBindingMetadata metadata = binding.getJmsMetadata();
        MessageSelection messageSelection = metadata.getMessageSelection();
        assertEquals("select", messageSelection.getSelector());
        assertEquals("val", messageSelection.getProperties().get("prop1"));

    }

    public void testBindingHandler() throws Exception {
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(BINDING_HANDLER.getBytes()));
        reader.nextTag();

        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        LoaderRegistry registry = EasyMock.createStrictMock(LoaderRegistry.class);

        URI uri = URI.create("TestHandler");
        EasyMock.expect(registry.load(reader, BindingHandlerDefinition.class, context)).andReturn(new BindingHandlerDefinition(uri));
        EasyMock.replay(helper, handlerRegistry, registry);

        JmsBindingLoader loader = new JmsBindingLoader(helper, registry);

        loader.load(reader, context);

        EasyMock.verify(helper, handlerRegistry, registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = XMLInputFactory.newInstance();

        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        LoaderRegistry registry = EasyMock.createNiceMock(LoaderRegistry.class);
        handlerRegistry = EasyMock.createNiceMock(BindingHandlerRegistry.class);
        EasyMock.replay(helper, registry);

        loader = new JmsBindingLoader(helper, registry);
        context = new DefaultIntrospectionContext();
    }
}

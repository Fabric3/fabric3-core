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
*/
package org.fabric3.fabric.deployment.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.deployment.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ConsumerDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ProducerDefinition;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.definitions.IntentMap;
import org.fabric3.api.model.type.definitions.PolicyPhase;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.deployment.generator.component.ComponentGenerator;
import org.fabric3.spi.deployment.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.deployment.generator.channel.EventStreamHandlerGenerator;
import org.fabric3.spi.deployment.generator.policy.PolicyMetadata;
import org.fabric3.spi.deployment.generator.policy.PolicyResolver;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaClass;
import org.w3c.dom.Element;

/**
 *
 */
public class ConnectionGeneratorImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionTargetDefinition targetDefinition = new MockPhysicalConnectionTargetDefinition();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(targetDefinition);

        EventStreamHandlerGenerator handlerGenerator = EasyMock.createMock(EventStreamHandlerGenerator.class);
        PhysicalHandlerDefinition handlerDefinition = new PhysicalHandlerDefinition();
        EasyMock.expect(handlerGenerator.generate(EasyMock.isA(Element.class), EasyMock.isA(PolicyMetadata.class))).andReturn(handlerDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        EasyMock.expect(generatorRegistry.getEventStreamHandlerGenerator(EasyMock.isA(QName.class))).andReturn(handlerGenerator);

        MockPolicyResult result = createMockPolicy();

        PolicyResolver resolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(resolver.resolvePolicies(EasyMock.isA(LogicalConsumer.class))).andReturn(result);

        EasyMock.replay(componentGenerator, handlerGenerator, generatorRegistry, resolver);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, resolver);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, false);
        Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<>();
        channels.put(channel, ChannelDeliveryType.DEFAULT);

        LogicalConsumer consumer = createConsumer(parent, "testChannel");

        List<PhysicalChannelConnectionDefinition> definitions = generator.generateConsumer(consumer, channels);
        assertEquals(1, definitions.size());
        PhysicalChannelConnectionDefinition definition = definitions.get(0);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getTarget());
        assertTrue(definition.getSource() instanceof ChannelSourceDefinition);
        assertNotNull(definition.getEventStream());

        EasyMock.verify(componentGenerator, handlerGenerator, generatorRegistry, resolver);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionTargetDefinition targetDefinition = new MockPhysicalConnectionTargetDefinition();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(targetDefinition);

        EventStreamHandlerGenerator handlerGenerator = EasyMock.createMock(EventStreamHandlerGenerator.class);
        PhysicalHandlerDefinition handlerDefinition = new PhysicalHandlerDefinition();
        EasyMock.expect(handlerGenerator.generate(EasyMock.isA(Element.class), EasyMock.isA(PolicyMetadata.class))).andReturn(handlerDefinition);

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        PhysicalConnectionSourceDefinition sourceDefinition = new PhysicalConnectionSourceDefinition();
        EasyMock.expect(bindingGenerator.generateConnectionSource(EasyMock.isA(LogicalConsumer.class),
                                                                  EasyMock.isA(LogicalBinding.class),
                                                                  EasyMock.isA(ChannelDeliveryType.class))).andReturn(sourceDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        EasyMock.expect(generatorRegistry.getEventStreamHandlerGenerator(EasyMock.isA(QName.class))).andReturn(handlerGenerator);
        generatorRegistry.getConnectionBindingGenerator(EasyMock.eq(MockBinding.class));
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        MockPolicyResult result = createMockPolicy();

        PolicyResolver resolver = EasyMock.createMock(PolicyResolver.class);
        EasyMock.expect(resolver.resolvePolicies(EasyMock.isA(LogicalConsumer.class))).andReturn(result);

        EasyMock.replay(componentGenerator, handlerGenerator, bindingGenerator, generatorRegistry, resolver);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, resolver);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, true);
        Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<>();
        channels.put(channel, ChannelDeliveryType.DEFAULT);

        LogicalConsumer consumer = createConsumer(parent, "testChannel");

        List<PhysicalChannelConnectionDefinition> definitions = generator.generateConsumer(consumer, channels);
        assertEquals(2, definitions.size());
        PhysicalChannelConnectionDefinition definition = definitions.get(0);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getEventStream());

        EasyMock.verify(componentGenerator, handlerGenerator, bindingGenerator, generatorRegistry, resolver);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalProducer() throws Exception {
        // Note this test should be updated to verify policy when the later is supported on producers
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionSourceDefinition sourceDefinition = new MockPhysicalConnectionSourceDefinition();
        EasyMock.expect(componentGenerator.generateConnectionSource(EasyMock.isA(LogicalProducer.class))).andReturn(sourceDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);

        PolicyResolver resolver = EasyMock.createMock(PolicyResolver.class);

        EasyMock.replay(componentGenerator, generatorRegistry, resolver);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, resolver);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, false);
        Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<>();
        channels.put(channel, ChannelDeliveryType.DEFAULT);

        LogicalProducer producer = createProducer(parent, "testChannel");

        List<PhysicalChannelConnectionDefinition> definitions = generator.generateProducer(producer, channels);
        assertEquals(1, definitions.size());
        PhysicalChannelConnectionDefinition definition = definitions.get(0);
        assertNotNull(definition.getSource());
        assertTrue(definition.getTarget() instanceof ChannelTargetDefinition);
        assertNotNull(definition.getEventStream());

        EasyMock.verify(componentGenerator, generatorRegistry, resolver);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundProducer() throws Exception {
        // Note this test should be updated to verify policy when the later is supported on producers
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionSourceDefinition sourceDefinition = new MockPhysicalConnectionSourceDefinition();
        EasyMock.expect(componentGenerator.generateConnectionSource(EasyMock.isA(LogicalProducer.class))).andReturn(sourceDefinition);

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        PhysicalConnectionTargetDefinition targetDefinition = new PhysicalConnectionTargetDefinition();
        EasyMock.expect(bindingGenerator.generateConnectionTarget(EasyMock.isA(LogicalProducer.class),
                                                                  EasyMock.isA(LogicalBinding.class),
                                                                  EasyMock.isA(ChannelDeliveryType.class))).andReturn(targetDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        generatorRegistry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        PolicyResolver resolver = EasyMock.createMock(PolicyResolver.class);

        EasyMock.replay(componentGenerator, bindingGenerator, generatorRegistry, resolver);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, resolver);
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, true);
        Map<LogicalChannel, ChannelDeliveryType> channels = new HashMap<>();
        channels.put(channel, ChannelDeliveryType.DEFAULT);

        LogicalProducer producer = createProducer(parent, "testChannel");

        List<PhysicalChannelConnectionDefinition> definitions = generator.generateProducer(producer, channels);
        assertEquals(2, definitions.size());
        PhysicalChannelConnectionDefinition definition = definitions.get(0);
        assertNotNull(definition.getSource());
        assertNotNull(definition.getEventStream());

        EasyMock.verify(componentGenerator, bindingGenerator, generatorRegistry, resolver);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalConsumer createConsumer(LogicalCompositeComponent parent, String channelName) {
        LogicalComponent<?> component = createComponent(parent);

        ConsumerDefinition consumerDefinition = new ConsumerDefinition("consumer");
        DataType javaClass = new JavaClass<>(Object.class);
        List list = Collections.singletonList(javaClass);
        consumerDefinition.setTypes(list);
        LogicalConsumer consumer = new LogicalConsumer(URI.create("composite/component#consumer"), consumerDefinition, component);
        consumer.addSource(URI.create(channelName));
        component.addConsumer(consumer);
        return consumer;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalProducer createProducer(LogicalCompositeComponent parent, String target) {
        LogicalComponent<?> component = createComponent(parent);

        ProducerDefinition producerDefinition = new ProducerDefinition("producer");
        LogicalProducer producer = new LogicalProducer(URI.create("composite/component#producer"), producerDefinition, component);
        DataType javaClass = new JavaClass<>(Object.class);
        List list = Collections.singletonList(javaClass);
        Operation operationDefinition = new Operation("operation", list, null, null);
        LogicalOperation operation = new LogicalOperation(operationDefinition, producer);
        producer.getOperations().add(operation);
        producer.addTarget(URI.create(target));
        component.addProducer(producer);
        return producer;
    }

    private LogicalChannel createChannel(LogicalCompositeComponent parent, boolean addBinding) {
        ChannelDefinition channelDefinition = new ChannelDefinition("testChannel", URI.create("contribution"));
        LogicalChannel channel = new LogicalChannel(URI.create("testChannel"), channelDefinition, parent);
        parent.addChannel(channel);
        if (addBinding) {
            MockBinding binding = new MockBinding();
            channel.addBinding(new LogicalBinding<>(binding, channel));
        }
        return channel;
    }

    private LogicalComponent<?> createComponent(LogicalCompositeComponent parent) {
        MockImplementation impl = new MockImplementation();
        ComponentDefinition<MockImplementation> definition = new ComponentDefinition<>("component");
        definition.setImplementation(impl);
        LogicalComponent<?> component = new LogicalComponent<>(URI.create("composite/component"), definition, parent);
        parent.addComponent(component);
        return component;
    }

    private MockPolicyResult createMockPolicy() {
        MockPolicyResult result = new MockPolicyResult();
        LogicalOperation operation = new LogicalOperation(new Operation(null, null, null, null), null);
        result.addMetadata(operation, new PolicyMetadata());
        QName setName = new QName("test", "setName");
        Set<QName> provided = Collections.singleton(new QName("test", "testPolicy"));
        Element expression = EasyMock.createMock(Element.class);
        EasyMock.expect(expression.getNamespaceURI()).andReturn("test");
        EasyMock.expect(expression.getLocalName()).andReturn("test");
        EasyMock.replay(expression);
        PolicySet policySet = new PolicySet(setName,
                                            provided,
                                            null,
                                            null,
                                            expression,
                                            PolicyPhase.INTERCEPTION,
                                            Collections.<IntentMap>emptySet(),
                                            URI.create("contribution"));
        result.addInterceptedPolicySets(operation, Collections.<PolicySet>singletonList(policySet));
        return result;
    }

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = -7252835651799578229L;

        @Override
        public QName getType() {
            return null;
        }
    }

    private class MockPhysicalConnectionTargetDefinition extends PhysicalConnectionTargetDefinition {
        private static final long serialVersionUID = 6229561274987180254L;
    }

    private class MockPhysicalConnectionSourceDefinition extends PhysicalConnectionSourceDefinition {
        private static final long serialVersionUID = 6229561274987180254L;
    }

}

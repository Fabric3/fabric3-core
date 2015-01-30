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
 */
package org.fabric3.fabric.domain.generator.channel;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ConsumerDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ProducerDefinition;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
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
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class ConnectionGeneratorImplTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionTargetDefinition targetDefinition = new MockPhysicalConnectionTargetDefinition();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(targetDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);

        EasyMock.replay(componentGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry);

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

        EasyMock.verify(componentGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionTargetDefinition targetDefinition = new MockPhysicalConnectionTargetDefinition();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(targetDefinition);

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        PhysicalConnectionSourceDefinition sourceDefinition = new PhysicalConnectionSourceDefinition();
        EasyMock.expect(bindingGenerator.generateConnectionSource(EasyMock.isA(LogicalConsumer.class),
                                                                  EasyMock.isA(LogicalBinding.class),
                                                                  EasyMock.isA(ChannelDeliveryType.class))).andReturn(sourceDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        generatorRegistry.getConnectionBindingGenerator(EasyMock.eq(MockBinding.class));
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(componentGenerator, bindingGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry);

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

        EasyMock.verify(componentGenerator, bindingGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalProducer() throws Exception {
        // Note this test should be updated to verify policy when the later is supported on producers
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockPhysicalConnectionSourceDefinition sourceDefinition = new MockPhysicalConnectionSourceDefinition();
        EasyMock.expect(componentGenerator.generateConnectionSource(EasyMock.isA(LogicalProducer.class))).andReturn(sourceDefinition);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);

        EasyMock.replay(componentGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry);

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

        EasyMock.verify(componentGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundProducer() throws Exception {
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

        EasyMock.replay(componentGenerator, bindingGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry);
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

        EasyMock.verify(componentGenerator, bindingGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalConsumer createConsumer(LogicalCompositeComponent parent, String channelName) {
        LogicalComponent<?> component = createComponent(parent);

        ConsumerDefinition consumerDefinition = new ConsumerDefinition("consumer");
        DataType javaClass = new JavaType(Object.class);
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
        DataType javaClass = new JavaType(Object.class);
        List list = Collections.singletonList(javaClass);
        Operation operationDefinition = new Operation("operation", list, null, null);
        LogicalOperation operation = new LogicalOperation(operationDefinition, producer);
        producer.getOperations().add(operation);
        producer.addTarget(URI.create(target));
        component.addProducer(producer);
        return producer;
    }

    private LogicalChannel createChannel(LogicalCompositeComponent parent, boolean addBinding) {
        ChannelDefinition channelDefinition = new ChannelDefinition("testChannel");
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

    private class MockImplementation extends Implementation<ComponentType> {
        private static final long serialVersionUID = -7252835651799578229L;

        @Override
        public String getType() {
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

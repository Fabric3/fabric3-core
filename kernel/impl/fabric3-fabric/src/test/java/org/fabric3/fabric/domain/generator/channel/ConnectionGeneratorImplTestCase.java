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
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSource;
import org.fabric3.fabric.model.physical.ChannelTarget;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class ConnectionGeneratorImplTestCase extends TestCase {

    private ClassLoaderRegistry classLoaderRegistry;

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockTarget target = new MockTarget();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(target);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);

        EasyMock.replay(componentGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, classLoaderRegistry);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, false);
        Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
        channels.put(channel, DeliveryType.DEFAULT);

        LogicalConsumer consumer = createConsumer(parent, "testChannel");

        List<PhysicalChannelConnection> connections = generator.generateConsumer(consumer, channels);
        assertEquals(1, connections.size());
        PhysicalChannelConnection connection = connections.get(0);
        assertNotNull(connection.getSource());
        assertNotNull(connection.getTarget());
        assertTrue(connection.getSource() instanceof ChannelSource);
        assertNotNull(connection.getEventType());

        EasyMock.verify(componentGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundConsumer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockTarget target = new MockTarget();
        EasyMock.expect(componentGenerator.generateConnectionTarget(EasyMock.isA(LogicalConsumer.class))).andReturn(target);

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        PhysicalConnectionSource source = new PhysicalConnectionSource();
        EasyMock.expect(bindingGenerator.generateConnectionSource(EasyMock.isA(LogicalConsumer.class),
                                                                  EasyMock.isA(LogicalBinding.class),
                                                                  EasyMock.isA(DeliveryType.class))).andReturn(source);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        generatorRegistry.getConnectionBindingGenerator(EasyMock.eq(MockBinding.class));
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(componentGenerator, bindingGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, classLoaderRegistry);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, true);
        Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
        channels.put(channel, DeliveryType.DEFAULT);

        LogicalConsumer consumer = createConsumer(parent, "testChannel");

        List<PhysicalChannelConnection> connections = generator.generateConsumer(consumer, channels);
        assertEquals(2, connections.size());
        PhysicalChannelConnection connection = connections.get(0);
        assertNotNull(connection.getSource());
        assertNotNull(connection.getEventType());

        EasyMock.verify(componentGenerator, bindingGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateLocalProducer() throws Exception {
        // Note this test should be updated to verify policy when the later is supported on producers
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockSource source = new MockSource();
        EasyMock.expect(componentGenerator.generateConnectionSource(EasyMock.isA(LogicalProducer.class))).andReturn(source);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);

        EasyMock.replay(componentGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, classLoaderRegistry);

        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, false);
        Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
        channels.put(channel, DeliveryType.DEFAULT);

        LogicalProducer producer = createProducer(parent, "testChannel");

        List<PhysicalChannelConnection> connections = generator.generateProducer(producer, channels);
        assertEquals(1, connections.size());
        PhysicalChannelConnection connection = connections.get(0);
        assertNotNull(connection.getSource());
        assertTrue(connection.getTarget() instanceof ChannelTarget);
        assertNotNull(connection.getEventType());

        EasyMock.verify(componentGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    public void testGenerateBoundProducer() throws Exception {
        ComponentGenerator<LogicalComponent<MockImplementation>> componentGenerator = EasyMock.createMock(ComponentGenerator.class);
        MockSource source = new MockSource();
        EasyMock.expect(componentGenerator.generateConnectionSource(EasyMock.isA(LogicalProducer.class))).andReturn(source);

        ConnectionBindingGenerator<?> bindingGenerator = EasyMock.createMock(ConnectionBindingGenerator.class);
        PhysicalConnectionTarget target = new PhysicalConnectionTarget();
        EasyMock.expect(bindingGenerator.generateConnectionTarget(EasyMock.isA(LogicalProducer.class),
                                                                  EasyMock.isA(LogicalBinding.class),
                                                                  EasyMock.isA(DeliveryType.class))).andReturn(target);

        GeneratorRegistry generatorRegistry = EasyMock.createMock(GeneratorRegistry.class);
        EasyMock.expect(generatorRegistry.getComponentGenerator(MockImplementation.class)).andReturn(componentGenerator);
        generatorRegistry.getConnectionBindingGenerator(MockBinding.class);
        EasyMock.expectLastCall().andReturn(bindingGenerator);

        EasyMock.replay(componentGenerator, bindingGenerator, generatorRegistry);

        ConnectionGeneratorImpl generator = new ConnectionGeneratorImpl(generatorRegistry, classLoaderRegistry);
        LogicalCompositeComponent parent = new LogicalCompositeComponent(URI.create("composite"), null, null);
        LogicalChannel channel = createChannel(parent, true);
        Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
        channels.put(channel, DeliveryType.DEFAULT);

        LogicalProducer producer = createProducer(parent, "testChannel");

        List<PhysicalChannelConnection> connections = generator.generateProducer(producer, channels);
        assertEquals(2, connections.size());
        PhysicalChannelConnection connection = connections.get(0);
        assertNotNull(connection.getSource());
        assertNotNull(connection.getEventType());

        EasyMock.verify(componentGenerator, bindingGenerator, generatorRegistry);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalConsumer createConsumer(LogicalCompositeComponent parent, String channelName) {
        LogicalComponent<?> component = createComponent(parent);

        Consumer consumer = new Consumer("consumer");
        DataType javaClass = new JavaType(Object.class);
        consumer.setType(javaClass);
        LogicalConsumer logicalConsumer = new LogicalConsumer(URI.create("composite/component#consumer"), consumer, component);
        logicalConsumer.addSource(URI.create(channelName));
        component.addConsumer(logicalConsumer);
        return logicalConsumer;
    }

    @SuppressWarnings({"unchecked"})
    private LogicalProducer createProducer(LogicalCompositeComponent parent, String target) {
        LogicalComponent<?> component = createComponent(parent);

        Producer producer = new Producer("producer");
        LogicalProducer logicalProducer = new LogicalProducer(URI.create("composite/component#producer"), producer, component);
        DataType javaClass = new JavaType(Object.class);
        List list = Collections.singletonList(javaClass);
        Operation operation = new Operation("operation", list, null, null);
        LogicalOperation logicalOperation = new LogicalOperation(operation, logicalProducer);
        logicalProducer.getOperations().add(logicalOperation);
        logicalProducer.addTarget(URI.create(target));
        component.addProducer(logicalProducer);
        return logicalProducer;
    }

    private LogicalChannel createChannel(LogicalCompositeComponent parent, boolean addBinding) {
        Channel channel = new Channel("testChannel");
        LogicalChannel logicalChannel = new LogicalChannel(URI.create("testChannel"), channel, parent);
        parent.addChannel(logicalChannel);
        if (addBinding) {
            MockBinding binding = new MockBinding();
            logicalChannel.addBinding(new LogicalBinding<>(binding, logicalChannel));
        }
        return logicalChannel;
    }

    private LogicalComponent<?> createComponent(LogicalCompositeComponent parent) {
        MockImplementation impl = new MockImplementation();
        Component<MockImplementation> component = new Component<>("component");
        component.setContributionUri(URI.create("test"));
        component.setImplementation(impl);
        LogicalComponent<?> logicalComponent = new LogicalComponent<>(URI.create("composite/component"), component, parent);
        parent.addComponent(logicalComponent);
        return logicalComponent;
    }

    @Override
    public void setUp() throws Exception {
        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(getClass().getClassLoader()).anyTimes();
        EasyMock.replay(classLoaderRegistry);

    }

    private class MockImplementation extends Implementation<ComponentType> {
        public String getType() {
            return null;
        }
    }

    private class MockTarget extends PhysicalConnectionTarget {
    }

    private class MockSource extends PhysicalConnectionSource {
    }

}

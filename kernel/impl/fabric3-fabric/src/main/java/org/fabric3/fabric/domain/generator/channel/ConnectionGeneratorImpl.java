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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.api.ChannelEvent;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSource;
import org.fabric3.fabric.model.physical.ChannelTarget;
import org.fabric3.fabric.model.physical.TypePhysicalEventFilter;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalInvocable;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalEventStream;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ConnectionGeneratorImpl implements ConnectionGenerator {
    private GeneratorRegistry generatorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    public ConnectionGeneratorImpl(@Reference GeneratorRegistry generatorRegistry, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.generatorRegistry = generatorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @SuppressWarnings({"unchecked"})
    public List<PhysicalChannelConnection> generateProducer(LogicalProducer producer, Map<LogicalChannel, DeliveryType> channels) {
        LogicalComponent<?> component = producer.getParent();
        ComponentGenerator<?> componentGenerator = getGenerator(component);
        PhysicalConnectionSource source = componentGenerator.generateConnectionSource(producer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        source.setClassLoader(classLoader);

        if (isDirect(producer, channels)) {
            source.setDirectConnection(true);
            return generateDirectConnections(producer, channels, source, classLoader);
        } else {
            return generateConnections(producer, channels, source, classLoader);
        }
    }

    public List<PhysicalChannelConnection> generateConsumer(LogicalConsumer consumer, Map<LogicalChannel, DeliveryType> channels) {
        LogicalComponent<?> component = consumer.getParent();
        ComponentGenerator<?> generator = getGenerator(component);
        PhysicalConnectionTarget target = generator.generateConnectionTarget(consumer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        target.setClassLoader(classLoader);

        if (isDirect(consumer, channels)) {
            target.setDirectConnection(true);
            return generateDirectConnections(consumer, channels, target, classLoader);
        } else {
            return generateConnections(consumer, channels, target, classLoader);
        }

    }

    private List<PhysicalChannelConnection> generateDirectConnections(LogicalConsumer consumer,
                                                                      Map<LogicalChannel, DeliveryType> channels,
                                                                      PhysicalConnectionTarget target,
                                                                      ClassLoader classLoader) {
        List<PhysicalChannelConnection> connections = new ArrayList<>();
        PhysicalEventStream eventStream = new PhysicalEventStream("stream");
        for (Map.Entry<LogicalChannel, DeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnection producerConnection = generateLocalConnection(consumer, channel, target, classLoader, eventStream);
                connections.add(producerConnection);
            } else {
                DeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnection bindingConnection = generateDirectBoundConnection(consumer, channel, deliveryType, classLoader, target, eventStream);
                connections.add(bindingConnection);
            }
        }
        return connections;
    }

    private List<PhysicalChannelConnection> generateConnections(LogicalConsumer consumer,
                                                                Map<LogicalChannel, DeliveryType> channels,
                                                                PhysicalConnectionTarget target,
                                                                ClassLoader classLoader) {
        List<PhysicalChannelConnection> connections = new ArrayList<>();
        PhysicalEventStream eventStream = generateEventStream(consumer);

        for (Map.Entry<LogicalChannel, DeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnection consumerConnection = generateLocalConnection(consumer, channel, target, classLoader, eventStream);
                connections.add(consumerConnection);
            } else {
                PhysicalChannelConnection consumerConnection = generateLocalConnection(consumer, channel, target, classLoader, eventStream);
                connections.add(consumerConnection);
                DeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnection bindingDefinition = generateBoundConnection(consumer, channel, deliveryType, classLoader, eventStream);
                connections.add(bindingDefinition);
            }

        }
        return connections;
    }

    private List<PhysicalChannelConnection> generateDirectConnections(LogicalProducer producer,
                                                                      Map<LogicalChannel, DeliveryType> channels,
                                                                      PhysicalConnectionSource source,
                                                                      ClassLoader classLoader) {
        List<PhysicalChannelConnection> connections = new ArrayList<>();
        PhysicalEventStream eventStream = new PhysicalEventStream("stream");
        for (Map.Entry<LogicalChannel, DeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnection producerConnection = generateLocalConnection(producer, channel, source, classLoader, eventStream);
                connections.add(producerConnection);
            } else {
                DeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnection bindingConnection = generateDirectBoundConnection(producer, channel, deliveryType, classLoader, source, eventStream);
                connections.add(bindingConnection);
            }
        }
        return connections;
    }

    private List<PhysicalChannelConnection> generateConnections(LogicalProducer producer,
                                                                Map<LogicalChannel, DeliveryType> channels,
                                                                PhysicalConnectionSource source,
                                                                ClassLoader classLoader) {
        List<PhysicalChannelConnection> connections = new ArrayList<>();

        PhysicalEventStream eventStream = generateEventStream(producer);

        for (Map.Entry<LogicalChannel, DeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnection connection = generateLocalConnection(producer, channel, source, classLoader, eventStream);
                connections.add(connection);
            } else {
                PhysicalChannelConnection producerConnection = generateLocalConnection(producer, channel, source, classLoader, eventStream);
                connections.add(producerConnection);
                DeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnection bindingConnection = generateBoundConnection(producer, channel, deliveryType, classLoader, eventStream);
                connections.add(bindingConnection);
            }

        }
        return connections;
    }

    private boolean isDirect(LogicalInvocable invocable, Map<LogicalChannel, DeliveryType> channels) {
        boolean direct = false;
        if (!channels.isEmpty()) {
            LogicalChannel logicalChannel = channels.keySet().iterator().next();
            ServiceContract contract = invocable.getServiceContract();
            if (contract == null) {
                return false;
            }
            Class<?> interfaceClass = contract.getInterfaceClass();
            if (logicalChannel.isBound()) {
                Binding binding = logicalChannel.getBinding().getDefinition();
                if (!binding.getConnectionTypes().isEmpty()) {
                    direct = binding.getConnectionTypes().stream().anyMatch(t -> t.isAssignableFrom(interfaceClass));
                }
            } else {
                Channel channel = logicalChannel.getDefinition();
                if (!channel.getConnectionTypes().isEmpty()) {
                    direct = channel.getConnectionTypes().stream().anyMatch(t -> t.isAssignableFrom(interfaceClass));
                }
            }
        }
        return direct;
    }

    private PhysicalChannelConnection generateLocalConnection(LogicalConsumer consumer,
                                                              LogicalChannel channel,
                                                              PhysicalConnectionTarget target,
                                                              ClassLoader classLoader,
                                                              PhysicalEventStream eventStream) {
        // the channel does not have bindings, which means it is a local channel
        boolean bound = channel.isBound();
        if (!channel.getZone().equals(consumer.getParent().getZone()) && !bound) {
            String name = channel.getDefinition().getName();
            throw new Fabric3Exception("Binding not configured on a channel where the consumer is in a different zone: " + name);
        }
        // construct a local connection to the channel
        URI uri = channel.getUri();
        PhysicalConnectionSource source = new ChannelSource(uri, ChannelSide.CONSUMER);
        source.setSequence(consumer.getDefinition().getSequence());
        source.setClassLoader(classLoader);
        return new PhysicalChannelConnection(uri, source, target, eventStream, bound);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnection generateBoundConnection(LogicalConsumer consumer,
                                                              LogicalChannel channel,
                                                              DeliveryType deliveryType,
                                                              ClassLoader classLoader,
                                                              PhysicalEventStream eventStream) {
        // use the bindings on the channel to create a consumer binding configuration
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionSource source = bindingGenerator.generateConnectionSource(consumer, binding, deliveryType);
        source.setSequence(consumer.getDefinition().getSequence());

        source.setClassLoader(classLoader);
        URI uri = channel.getUri();
        ChannelTarget target = new ChannelTarget(uri, ChannelSide.CONSUMER);
        target.setClassLoader(classLoader);
        return new PhysicalChannelConnection(uri, source, target, eventStream, true);
    }

    private PhysicalChannelConnection generateLocalConnection(LogicalProducer producer,
                                                              LogicalChannel channel,
                                                              PhysicalConnectionSource source,
                                                              ClassLoader classLoader,
                                                              PhysicalEventStream eventStream) {
        boolean bound = channel.isBound();
        if (!channel.getZone().equals(producer.getParent().getZone()) && !bound) {
            String name = channel.getDefinition().getName();
            throw new Fabric3Exception("Binding not configured on a channel where the producer is in a different zone: " + name);
        }
        URI uri = channel.getUri();
        PhysicalConnectionTarget target = new ChannelTarget(uri, ChannelSide.PRODUCER);

        target.setClassLoader(classLoader);

        return new PhysicalChannelConnection(uri, source, target, eventStream, bound);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnection generateBoundConnection(LogicalProducer producer,
                                                              LogicalChannel channel,
                                                              DeliveryType deliveryType,
                                                              ClassLoader classLoader,
                                                              PhysicalEventStream eventStream) {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionTarget target = bindingGenerator.generateConnectionTarget(producer, binding, deliveryType);
        target.setClassLoader(classLoader);
        URI uri = channel.getUri();
        ChannelSource source = new ChannelSource(uri, ChannelSide.PRODUCER);
        source.setClassLoader(classLoader);
        boolean bound = channel.isBound();
        return new PhysicalChannelConnection(uri, source, target, eventStream, bound);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnection generateDirectBoundConnection(LogicalConsumer consumer,
                                                                    LogicalChannel channel,
                                                                    DeliveryType deliveryType,
                                                                    ClassLoader classLoader,
                                                                    PhysicalConnectionTarget target,
                                                                    PhysicalEventStream eventStream) {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionSource source = bindingGenerator.generateConnectionSource(consumer, binding, deliveryType);
        source.setClassLoader(classLoader);
        URI uri = channel.getUri();
        return new PhysicalChannelConnection(uri, source, target, eventStream, true);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnection generateDirectBoundConnection(LogicalProducer producer,
                                                                    LogicalChannel channel,
                                                                    DeliveryType deliveryType,
                                                                    ClassLoader classLoader,
                                                                    PhysicalConnectionSource source,
                                                                    PhysicalEventStream eventStream) {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionTarget target = bindingGenerator.generateConnectionTarget(producer, binding, deliveryType);
        target.setClassLoader(classLoader);
        URI uri = channel.getUri();
        return new PhysicalChannelConnection(uri, source, target, eventStream, true);
    }

    private PhysicalEventStream generateEventStream(LogicalProducer producer) {
        Operation operation = producer.getStreamOperation().getDefinition();
        PhysicalEventStream physicalStream = new PhysicalEventStream(operation.getName());
        physicalStream.setName(operation.getName());
        List<DataType> params = operation.getInputTypes();
        if (params.size() < 1) {
            String interfaceName = producer.getServiceContract().getQualifiedInterfaceName();
            throw new Fabric3Exception("A channel interface must have one parameter: operation " + operation.getName() + " on " + interfaceName);
        }
        for (DataType param : params) {
            Class<?> paramType = param.getType();
            physicalStream.addEventType(paramType);
        }
        return physicalStream;
    }

    private PhysicalEventStream generateEventStream(LogicalConsumer consumer) {
        PhysicalEventStream physicalStream = new PhysicalEventStream("default");
        Consumer<?> consumerDefinition = consumer.getDefinition();
        List<DataType> types = consumerDefinition.getTypes();
        boolean typed = false;
        boolean takesChannelEvent = false;
        for (DataType dataType : types) {
            if (dataType instanceof JavaType) {
                // for now only support Java contracts
                if (!Object.class.equals(dataType.getType())) {
                    typed = true;
                    if (ChannelEvent.class.isAssignableFrom(dataType.getType())) {
                        takesChannelEvent = true;
                    }
                }
            }
            physicalStream.setChannelEvent(takesChannelEvent);
            physicalStream.addEventType(dataType.getType());
        }
        if (typed) {
            TypePhysicalEventFilter typeFilter = new TypePhysicalEventFilter(types);
            physicalStream.addFilter(typeFilter);
        }
        return physicalStream;
    }

    @SuppressWarnings("unchecked")
    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return (ComponentGenerator<C>) generatorRegistry.getComponentGenerator(implementation.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends Binding> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}

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
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.fabric.model.physical.TypeEventFilterDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
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
    public List<PhysicalChannelConnectionDefinition> generateProducer(LogicalProducer producer, Map<LogicalChannel, ChannelDeliveryType> channels) {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<>();

        LogicalComponent<?> component = producer.getParent();
        ComponentGenerator<?> componentGenerator = getGenerator(component);
        PhysicalConnectionSourceDefinition sourceDefinition = componentGenerator.generateConnectionSource(producer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        sourceDefinition.setClassLoader(classLoader);

        PhysicalEventStreamDefinition eventStream = generateEventStream(producer);

        for (Map.Entry<LogicalChannel, ChannelDeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnectionDefinition definition = generateProducerConnection(producer, channel, sourceDefinition, classLoaderId, eventStream);
                definitions.add(definition);
            } else {
                PhysicalChannelConnectionDefinition producerDefinition = generateProducerConnection(producer,
                                                                                                    channel,
                                                                                                    sourceDefinition,
                                                                                                    classLoaderId,
                                                                                                    eventStream);
                definitions.add(producerDefinition);
                ChannelDeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnectionDefinition bindingDefinition = generateProducerBinding(producer, channel, deliveryType, classLoaderId, eventStream);
                definitions.add(bindingDefinition);
            }

        }
        return definitions;
    }

    public List<PhysicalChannelConnectionDefinition> generateConsumer(LogicalConsumer consumer, Map<LogicalChannel, ChannelDeliveryType> channels) {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<>();
        LogicalComponent<?> component = consumer.getParent();

        ComponentGenerator<?> generator = getGenerator(component);

        PhysicalConnectionTargetDefinition targetDefinition = generator.generateConnectionTarget(consumer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        targetDefinition.setClassLoader(classLoader);

        PhysicalEventStreamDefinition eventStream = generateEventStream(consumer);

        for (Map.Entry<LogicalChannel, ChannelDeliveryType> entry : channels.entrySet()) {
            LogicalChannel channel = entry.getKey();
            if (!channel.isBound()) {
                PhysicalChannelConnectionDefinition definition = generateConsumerConnection(consumer, channel, targetDefinition, classLoaderId, eventStream);
                definitions.add(definition);

            } else {
                PhysicalChannelConnectionDefinition consumerConnection = generateConsumerConnection(consumer,
                                                                                                    channel,
                                                                                                    targetDefinition,
                                                                                                    classLoaderId,
                                                                                                    eventStream);
                definitions.add(consumerConnection);
                ChannelDeliveryType deliveryType = entry.getValue();
                PhysicalChannelConnectionDefinition bindingDefinition = generateConsumerBinding(consumer, channel, deliveryType, classLoaderId, eventStream);
                definitions.add(bindingDefinition);
            }

        }
        return definitions;

    }

    private PhysicalChannelConnectionDefinition generateConsumerConnection(LogicalConsumer consumer,
                                                                           LogicalChannel channel,
                                                                           PhysicalConnectionTargetDefinition targetDefinition,
                                                                           URI classLoaderId,
                                                                           PhysicalEventStreamDefinition eventStream) {
        // the channel does not have bindings, which means it is a local channel
        if (!channel.getZone().equals(consumer.getParent().getZone()) && !channel.isBound()) {
            String name = channel.getDefinition().getName();
            throw new Fabric3Exception("Binding not configured on a channel where the consumer is in a different zone: " + name);
        }
        // construct a local connection to the channel
        PhysicalConnectionSourceDefinition sourceDefinition = new ChannelSourceDefinition(channel.getUri(), ChannelSide.CONSUMER);
        sourceDefinition.setSequence(consumer.getDefinition().getSequence());
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        sourceDefinition.setClassLoader(classLoader);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnectionDefinition generateConsumerBinding(LogicalConsumer consumer,
                                                                        LogicalChannel channel,
                                                                        ChannelDeliveryType deliveryType,
                                                                        URI classLoaderId,
                                                                        PhysicalEventStreamDefinition eventStream) {
        // use the bindings on the channel to create a consumer binding configuration
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionSourceDefinition sourceDefinition = bindingGenerator.generateConnectionSource(consumer, binding, deliveryType);
        sourceDefinition.setSequence(consumer.getDefinition().getSequence());

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

        sourceDefinition.setClassLoader(classLoader);
        ChannelTargetDefinition targetDefinition = new ChannelTargetDefinition(channel.getUri(), ChannelSide.CONSUMER);
        targetDefinition.setClassLoader(classLoader);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    private PhysicalChannelConnectionDefinition generateProducerConnection(LogicalProducer producer,
                                                                           LogicalChannel channel,
                                                                           PhysicalConnectionSourceDefinition sourceDefinition,
                                                                           URI classLoaderId,
                                                                           PhysicalEventStreamDefinition eventStream) {
        if (!channel.getZone().equals(producer.getParent().getZone()) && !channel.isBound()) {
            String name = channel.getDefinition().getName();
            throw new Fabric3Exception("Binding not configured on a channel where the producer is in a different zone: " + name);
        }
        PhysicalConnectionTargetDefinition targetDefinition = new ChannelTargetDefinition(channel.getUri(), ChannelSide.PRODUCER);

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        targetDefinition.setClassLoader(classLoader);

        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnectionDefinition generateProducerBinding(LogicalProducer producer,
                                                                        LogicalChannel channel,
                                                                        ChannelDeliveryType deliveryType,
                                                                        URI classLoaderId,
                                                                        PhysicalEventStreamDefinition eventStream) {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionTargetDefinition targetDefinition = bindingGenerator.generateConnectionTarget(producer, binding, deliveryType);
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        targetDefinition.setClassLoader(classLoader);
        ChannelSourceDefinition sourceDefinition = new ChannelSourceDefinition(channel.getUri(), ChannelSide.PRODUCER);
        sourceDefinition.setClassLoader(classLoader);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    private PhysicalEventStreamDefinition generateEventStream(LogicalProducer producer) {
        Operation operation = producer.getStreamOperation().getDefinition();
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition(operation.getName());
        definition.setName(operation.getName());
        List<DataType> params = operation.getInputTypes();
        if (params.size() < 1) {
            String interfaceName = producer.getServiceContract().getQualifiedInterfaceName();
            throw new Fabric3Exception("A channel interface must have one parameter: operation " + operation.getName() + " on " + interfaceName);
        }
        for (DataType param : params) {
            Class<?> paramType = param.getType();
            String paramName = paramType.getName();
            definition.addEventType(paramName);
        }
        return definition;
    }

    private PhysicalEventStreamDefinition generateEventStream(LogicalConsumer consumer) {
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition("default");
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
            definition.setChannelEvent(takesChannelEvent);
            definition.addEventType(dataType.getType().getName());
        }
        if (typed) {
            TypeEventFilterDefinition typeFilter = new TypeEventFilterDefinition(types);
            definition.addFilterDefinition(typeFilter);
        }
        return definition;
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

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.api.ChannelEvent;
import org.fabric3.fabric.deployment.generator.GeneratorNotFoundException;
import org.fabric3.fabric.deployment.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.fabric.model.physical.TypeEventFilterDefinition;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.deployment.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.deployment.generator.channel.ConnectionGenerator;
import org.fabric3.spi.deployment.generator.channel.EventStreamHandlerGenerator;
import org.fabric3.spi.deployment.generator.component.ComponentGenerator;
import org.fabric3.spi.deployment.generator.policy.PolicyMetadata;
import org.fabric3.spi.deployment.generator.policy.PolicyResolver;
import org.fabric3.spi.deployment.generator.policy.PolicyResult;
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
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ConnectionGeneratorImpl implements ConnectionGenerator {
    private GeneratorRegistry generatorRegistry;
    private PolicyResolver resolver;

    public ConnectionGeneratorImpl(@Reference GeneratorRegistry generatorRegistry, @Reference PolicyResolver resolver) {
        this.generatorRegistry = generatorRegistry;
        this.resolver = resolver;
    }

    @SuppressWarnings({"unchecked"})
    public List<PhysicalChannelConnectionDefinition> generateProducer(LogicalProducer producer, Map<LogicalChannel, ChannelDeliveryType> channels)
            throws GenerationException {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<>();

        LogicalComponent<?> component = producer.getParent();
        ComponentGenerator<?> componentGenerator = getGenerator(component);
        PhysicalConnectionSourceDefinition sourceDefinition = componentGenerator.generateConnectionSource(producer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        sourceDefinition.setClassLoaderId(classLoaderId);

        PhysicalEventStreamDefinition eventStream = generateProducerOperation(producer);

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

    public List<PhysicalChannelConnectionDefinition> generateConsumer(LogicalConsumer consumer, Map<LogicalChannel, ChannelDeliveryType> channels)
            throws GenerationException {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<>();
        LogicalComponent<?> component = consumer.getParent();

        ComponentGenerator<?> generator = getGenerator(component);

        PhysicalConnectionTargetDefinition targetDefinition = generator.generateConnectionTarget(consumer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        targetDefinition.setClassLoaderId(classLoaderId);

        PhysicalEventStreamDefinition eventStream = generateEventStream(consumer);

        generatePolicy(consumer, eventStream);
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
                                                                           PhysicalEventStreamDefinition eventStream) throws GenerationException {
        // the channel does not have bindings, which means it is a local channel
        if (!channel.getZone().equals(consumer.getParent().getZone()) && !channel.isBound()) {
            String name = channel.getDefinition().getName();
            throw new GenerationException("Binding not configured on a channel where the consumer is in a different zone: " + name);
        }
        // construct a local connection to the channel
        PhysicalConnectionSourceDefinition sourceDefinition = new ChannelSourceDefinition(channel.getUri(), ChannelSide.CONSUMER);
        sourceDefinition.setSequence(consumer.getDefinition().getSequence());
        sourceDefinition.setClassLoaderId(classLoaderId);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnectionDefinition generateConsumerBinding(LogicalConsumer consumer,
                                                                        LogicalChannel channel,
                                                                        ChannelDeliveryType deliveryType,
                                                                        URI classLoaderId,
                                                                        PhysicalEventStreamDefinition eventStream) throws GenerationException {
        // use the bindings on the channel to create a consumer binding configuration
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionSourceDefinition sourceDefinition = bindingGenerator.generateConnectionSource(consumer, binding, deliveryType);
        sourceDefinition.setSequence(consumer.getDefinition().getSequence());
        sourceDefinition.setClassLoaderId(classLoaderId);
        ChannelTargetDefinition targetDefinition = new ChannelTargetDefinition(channel.getUri(), ChannelSide.CONSUMER);
        targetDefinition.setClassLoaderId(classLoaderId);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    private PhysicalChannelConnectionDefinition generateProducerConnection(LogicalProducer producer,
                                                                           LogicalChannel channel,
                                                                           PhysicalConnectionSourceDefinition sourceDefinition,
                                                                           URI classLoaderId,
                                                                           PhysicalEventStreamDefinition eventStream) throws GenerationException {
        if (!channel.getZone().equals(producer.getParent().getZone()) && !channel.isBound()) {
            String name = channel.getDefinition().getName();
            throw new GenerationException("Binding not configured on a channel where the producer is in a different zone: " + name);
        }
        PhysicalConnectionTargetDefinition targetDefinition = new ChannelTargetDefinition(channel.getUri(), ChannelSide.PRODUCER);
        targetDefinition.setClassLoaderId(classLoaderId);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelConnectionDefinition generateProducerBinding(LogicalProducer producer,
                                                                        LogicalChannel channel,
                                                                        ChannelDeliveryType deliveryType,
                                                                        URI classLoaderId,
                                                                        PhysicalEventStreamDefinition eventStream) throws GenerationException {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionTargetDefinition targetDefinition = bindingGenerator.generateConnectionTarget(producer, binding, deliveryType);
        targetDefinition.setClassLoaderId(classLoaderId);
        ChannelSourceDefinition sourceDefinition = new ChannelSourceDefinition(channel.getUri(), ChannelSide.PRODUCER);
        sourceDefinition.setClassLoaderId(classLoaderId);
        return new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStream);
    }

    private void generatePolicy(LogicalConsumer consumer, PhysicalEventStreamDefinition eventStream) throws GenerationException {
        PolicyResult result = resolver.resolvePolicies(consumer);
        if (result.getInterceptedPolicySets().isEmpty()) {
            return;
        }
        List<PolicySet> policies = result.getInterceptedPolicySets().values().iterator().next();
        PolicyMetadata metadata = result.getMetadata().values().iterator().next();
        for (PolicySet set : policies) {
            QName expressionName = set.getExpressionName();
            EventStreamHandlerGenerator handlerGenerator = generatorRegistry.getEventStreamHandlerGenerator(expressionName);
            PhysicalHandlerDefinition definition = handlerGenerator.generate(set.getExpression(), metadata);
            if (definition != null) {
                definition.setPolicyClassLoaderId(set.getContributionUri());
                eventStream.addHandlerDefinition(definition);
            }
        }
    }

    private PhysicalEventStreamDefinition generateProducerOperation(LogicalProducer producer) throws GenerationException {
        Operation operation = producer.getStreamOperation().getDefinition();
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition(operation.getName());
        definition.setName(operation.getName());
        List<DataType<?>> params = operation.getInputTypes();
        if (params.size() < 1) {
            String interfaceName = producer.getServiceContract().getQualifiedInterfaceName();
            throw new GenerationException("A channel interface must have one parameter: operation " + operation.getName() + " on " + interfaceName);
        }
        for (DataType<?> param : params) {
            Class<?> paramType = param.getPhysical();
            String paramName = paramType.getName();
            definition.addEventType(paramName);
        }
        return definition;
    }

    private PhysicalEventStreamDefinition generateEventStream(LogicalConsumer consumer) {
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition("default");
        List<DataType<?>> types = consumer.getDefinition().getTypes();
        boolean typed = false;
        boolean takesChannelEvent = false;
        for (DataType<?> dataType : types) {
            if (dataType instanceof JavaType) {
                // for now only support Java contracts
                if (!Object.class.equals(dataType.getPhysical())) {
                    typed = true;
                    if (ChannelEvent.class.isAssignableFrom(dataType.getPhysical())) {
                        takesChannelEvent = true;
                    }
                }
            }
            definition.setChannelEvent(takesChannelEvent);
            definition.addEventType(dataType.getPhysical().getName());
        }
        if (typed) {
            TypeEventFilterDefinition typeFilter = new TypeEventFilterDefinition(types);
            definition.addFilterDefinition(typeFilter);
        }
        return definition;
    }

    @SuppressWarnings("unchecked")
    private <C extends LogicalComponent<?>> ComponentGenerator<C> getGenerator(C component) throws GeneratorNotFoundException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        return (ComponentGenerator<C>) generatorRegistry.getComponentGenerator(implementation.getClass());
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}

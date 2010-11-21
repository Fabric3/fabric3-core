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
package org.fabric3.fabric.generator.channel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.fabric.model.physical.ChannelTargetDefinition;
import org.fabric3.fabric.model.physical.TypeEventFilterDefinition;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.EventStreamHandlerGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.policy.PolicyMetadata;
import org.fabric3.spi.generator.policy.PolicyResolver;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.physical.PhysicalHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * @version $Rev$ $Date$
 */
public class ConnectionGeneratorImpl implements ConnectionGenerator {
    private GeneratorRegistry generatorRegistry;
    private PolicyResolver resolver;

    public ConnectionGeneratorImpl(@Reference GeneratorRegistry generatorRegistry, @Reference PolicyResolver resolver) {
        this.generatorRegistry = generatorRegistry;
        this.resolver = resolver;
    }

    @SuppressWarnings({"unchecked"})
    public List<PhysicalChannelConnectionDefinition> generateProducer(LogicalProducer producer) throws GenerationException {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<PhysicalChannelConnectionDefinition>();
        LogicalComponent<?> component = producer.getParent();
        ComponentGenerator<?> componentGenerator = getGenerator(component);
        PhysicalConnectionSourceDefinition sourceDefinition = componentGenerator.generateConnectionSource(producer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        sourceDefinition.setClassLoaderId(classLoaderId);

        List<PhysicalEventStreamDefinition> eventStreams = new ArrayList<PhysicalEventStreamDefinition>();

        for (LogicalOperation operation : producer.getOperations()) {
            eventStreams.add(generate(operation));
        }

        for (URI uri : producer.getTargets()) {
            LogicalChannel channel = getChannelInHierarchy(uri, producer);
            if (channel.getBindings().isEmpty()) {
                if (!channel.getZone().equals(producer.getParent().getZone())) {
                    throw new AssertionError("Binding not configured on a channel where the producer is in a different zone");
                }
                PhysicalConnectionTargetDefinition targetDefinition = new ChannelTargetDefinition(uri);
                targetDefinition.setClassLoaderId(classLoaderId);
                PhysicalChannelConnectionDefinition connectionDefinition =
                        new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStreams);
                definitions.add(connectionDefinition);
            } else {
                List<LogicalBinding<?>> bindings = channel.getBindings();
                LogicalBinding<?> binding = bindings.get(0);
                generateProducerBinding(producer, binding, sourceDefinition, eventStreams, classLoaderId, definitions);
            }
        }
        return definitions;
    }

    @SuppressWarnings({"unchecked"})
    public List<PhysicalChannelConnectionDefinition> generateConsumer(LogicalConsumer consumer) throws GenerationException {
        List<PhysicalChannelConnectionDefinition> definitions = new ArrayList<PhysicalChannelConnectionDefinition>();
        LogicalComponent<?> component = consumer.getParent();

        ComponentGenerator<?> generator = getGenerator(component);

        PhysicalConnectionTargetDefinition targetDefinition = generator.generateConnectionTarget(consumer);
        URI classLoaderId = component.getDefinition().getContributionUri();
        targetDefinition.setClassLoaderId(classLoaderId);

        List<PhysicalEventStreamDefinition> eventStreams = generateEventStreams(consumer);

        generatePolicy(consumer, eventStreams);

        for (URI uri : consumer.getSources()) {
            LogicalChannel channel = getChannelInHierarchy(uri, consumer);
            if (channel.getBindings().isEmpty()) {
                // the channel does not have bindings, which means it is a local channel
                if (!channel.getZone().equals(consumer.getParent().getZone())) {
                    throw new AssertionError("Binding not configured on a channel where the producer is in a different zone");
                }
                // construct a local connection to the channel
                PhysicalConnectionSourceDefinition sourceDefinition = new ChannelSourceDefinition(uri);
                sourceDefinition.setClassLoaderId(classLoaderId);
                PhysicalChannelConnectionDefinition connectionDefinition =
                        new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStreams);
                definitions.add(connectionDefinition);
            } else {
                // use the bindings on the channel to create a consumer binding configuration
                List<LogicalBinding<?>> bindings = channel.getBindings();
                LogicalBinding<?> binding = bindings.get(0);
                generateConsumerBinding(consumer, binding, targetDefinition, eventStreams, classLoaderId, definitions);
            }
        }
        return definitions;
    }

    private void generatePolicy(LogicalConsumer consumer, List<PhysicalEventStreamDefinition> eventStreams) throws GenerationException {
        PolicyResult result = resolver.resolvePolicies(consumer);
        List<PolicySet> policies = result.getInterceptedPolicySets().values().iterator().next();
        PolicyMetadata metadata = result.getMetadata().values().iterator().next();
        for (PolicySet set : policies) {
            QName expressionName = set.getExpressionName();
            EventStreamHandlerGenerator handlerGenerator = generatorRegistry.getEventStreamHandlerGenerator(expressionName);
            PhysicalHandlerDefinition definition = handlerGenerator.generate(set.getExpression(), metadata);
            if (definition != null) {
                definition.setPolicyClassLoaderId(set.getContributionUri());
                for (PhysicalEventStreamDefinition streamDefinition : eventStreams) {
                    streamDefinition.addHandlerDefinition(definition);
                }
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private void generateConsumerBinding(LogicalConsumer consumer,
                                         LogicalBinding binding,
                                         PhysicalConnectionTargetDefinition targetDefinition,
                                         List<PhysicalEventStreamDefinition> eventStreams,
                                         URI classLoaderId,
                                         List<PhysicalChannelConnectionDefinition> definitions) throws GenerationException {
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionSourceDefinition sourceDefinition = bindingGenerator.generateConnectionSource(consumer, binding);
        sourceDefinition.setClassLoaderId(classLoaderId);
        PhysicalChannelConnectionDefinition connectionDefinition =
                new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStreams);
        definitions.add(connectionDefinition);
    }

    @SuppressWarnings({"unchecked"})
    private void generateProducerBinding(LogicalProducer producer,
                                         LogicalBinding binding,
                                         PhysicalConnectionSourceDefinition sourceDefinition,
                                         List<PhysicalEventStreamDefinition> eventStreams,
                                         URI classLoaderId,
                                         List<PhysicalChannelConnectionDefinition> definitions) throws GenerationException {
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        PhysicalConnectionTargetDefinition targetDefinition = bindingGenerator.generateConnectionTarget(producer, binding);
        targetDefinition.setClassLoaderId(classLoaderId);
        PhysicalChannelConnectionDefinition connectionDefinition =
                new PhysicalChannelConnectionDefinition(sourceDefinition, targetDefinition, eventStreams);
        definitions.add(connectionDefinition);
    }

    private PhysicalEventStreamDefinition generate(LogicalOperation operation) {
        Operation o = operation.getDefinition();
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition(o.getName());
        definition.setName(o.getName());
        List<DataType<?>> params = o.getInputTypes();
        for (DataType<?> param : params) {
            Class<?> paramType = param.getPhysical();
            String paramName = paramType.getName();
            definition.addEventType(paramName);
        }
        return definition;
    }

    private List<PhysicalEventStreamDefinition> generateEventStreams(LogicalConsumer consumer) {
        // there is only one event stream from a channel to a consumer
        List<PhysicalEventStreamDefinition> streams = new ArrayList<PhysicalEventStreamDefinition>();
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition("default");
        List<DataType<?>> types = consumer.getDefinition().getTypes();
        boolean typed = false;
        for (DataType<?> dataType : types) {
            if (dataType instanceof JavaType) {
                // for now only support Java contracts
                if (!Object.class.equals(dataType.getLogical())) {
                    typed = true;
                }
            }
            definition.addEventType(dataType.getPhysical().getName());
        }
        if (typed) {
            TypeEventFilterDefinition typeFilter = new TypeEventFilterDefinition(types);
            definition.addFilterDefinition(typeFilter);
        }
        streams.add(definition);
        return streams;
    }

    private LogicalChannel getChannelInHierarchy(URI channelUri, Bindable bindable) throws GenerationException {
        LogicalChannel channel = null;
        while (true) {
            LogicalCompositeComponent parent = bindable.getParent().getParent();
            if (parent != null) {
                channel = parent.getChannel(channelUri);
                if (channel != null) {
                    break;
                }
            } else {
                break;
            }
        }
        if (channel == null) {
            throw new ChannelNotFoundException("Channel not found: " + channelUri);
        }
        return channel;
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

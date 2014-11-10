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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.ResourceReferenceDefinition;

/**
 * Copies a logical model graph.
 */
public class CopyUtil {

    private CopyUtil() {
    }

    /**
     * Makes a replica of the composite, including preservation of parent-child relationships.
     *
     * @param composite the composite to copy
     * @return the copy
     */
    public static LogicalCompositeComponent copy(LogicalCompositeComponent composite) {
        return copy(composite, composite.getParent());
    }

    /**
     * Performs the copy using depth-first traversal.
     *
     * @param composite the composite to copy
     * @param parent    the parent of the copy
     * @return the copy
     */
    private static LogicalCompositeComponent copy(LogicalCompositeComponent composite, LogicalCompositeComponent parent) {
        // Create maps to de-reference pointers to components, reference and services. Since the copy is performed depth-first, the maps
        // will always be populated before a component, reference, or service needs to be de-referenced.
        Map<URI, LogicalComponent<?>> components = new HashMap<>();
        Map<URI, LogicalReference> references = new HashMap<>();
        Map<URI, LogicalService> services = new HashMap<>();
        LogicalCompositeComponent replica = copy(composite, parent, components, services, references);

        // Wires must be copied last since they may contain forward references to services provided by components not yet copied. This
        // guarantees that all components and services will have been copied before wires are copied.
        copyWires(composite, components, services);
        return replica;
    }

    private static LogicalCompositeComponent copy(LogicalCompositeComponent composite,
                                                  LogicalCompositeComponent parent,
                                                  Map<URI, LogicalComponent<?>> components,
                                                  Map<URI, LogicalService> services,
                                                  Map<URI, LogicalReference> references) {

        URI uri = composite.getUri();
        ComponentDefinition<CompositeImplementation> definition = composite.getDefinition();
        LogicalCompositeComponent copy = new LogicalCompositeComponent(uri, definition, parent);
        components.put(uri, copy);
        copy.setAutowire(composite.getAutowire());
        copy.setState(composite.getState());
        copy.setZone(composite.getZone());
        copy.setDeployable(composite.getDeployable());
        copy.addIntents(composite.getIntents());
        copy.addPolicySets(composite.getPolicySets());
        for (LogicalProperty property : composite.getAllProperties().values()) {
            copy.setProperties(property);
        }
        for (LogicalComponent<?> component : composite.getComponents()) {
            copy(component, copy, components, services, references);
        }
        for (LogicalReference reference : composite.getReferences()) {
            copy(reference, copy, references);
        }
        for (LogicalResourceReference<?> resourceReference : composite.getResourceReferences()) {
            copy(resourceReference, copy);
        }
        for (LogicalService service : composite.getServices()) {
            copy(service, copy, components, services);
        }
        for (LogicalChannel channel : composite.getChannels()) {
            copy(channel, copy);
        }
        for (LogicalConsumer consumer : composite.getConsumers()) {
            copy(consumer, copy);
        }
        for (LogicalProducer producer : composite.getProducers()) {
            copy(producer, copy);
        }
        for (LogicalResource resource : composite.getResources()) {
            copy(resource, copy);
        }
        return copy;
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalComponent<?> component,
                             LogicalCompositeComponent newParent,
                             Map<URI, LogicalComponent<?>> components,
                             Map<URI, LogicalService> services,
                             Map<URI, LogicalReference> references) {
        LogicalComponent<?> copy;
        if (component instanceof LogicalCompositeComponent) {
            copy = copy((LogicalCompositeComponent) component, newParent, components, services, references);
        } else {
            URI uri = component.getUri();
            copy = new LogicalComponent(uri, component.getDefinition(), newParent);
            copy.setAutowire(component.getAutowire());
            copy.setState(component.getState());
            copy.setZone(component.getZone());
            copy.setDeployable(component.getDeployable());
            copy.addIntents(component.getIntents());
            copy.addPolicySets(component.getPolicySets());
            components.put(uri, copy);

            for (LogicalProperty property : component.getAllProperties().values()) {
                copy.setProperties(property);
            }
            for (LogicalReference reference : component.getReferences()) {
                copy(reference, copy, references);
            }
            for (LogicalResourceReference<?> resourceReference : component.getResourceReferences()) {
                copy(resourceReference, copy);
            }
            for (LogicalService service : component.getServices()) {
                copy(service, copy, components, services);
            }
            for (LogicalConsumer consumer : component.getConsumers()) {
                copy(consumer, copy);
            }
            for (LogicalProducer producer : component.getProducers()) {
                copy(producer, copy);
            }
        }
        newParent.addComponent(copy);
    }

    private static void copy(LogicalReference reference, LogicalComponent parent, Map<URI, LogicalReference> references) {
        URI referenceUri = reference.getUri();
        LogicalReference copy = new LogicalReference(referenceUri, reference.getDefinition(), parent);
        references.put(referenceUri, copy);
        for (URI uri : reference.getPromotedUris()) {
            copy.addPromotedUri(uri);
        }
        copy.setAutowire(reference.getAutowire());
        copy.setLeafReference(references.get(reference.getLeafReference().getUri()));
        copy.setResolved(reference.isResolved());
        copy.setServiceContract(reference.getServiceContract());
        for (URI uri : reference.getPromotedUris()) {
            copy.addPromotedUri(uri);
        }
        copy.addIntents(reference.getIntents());
        copy.addPolicySets(reference.getPolicySets());
        copy(reference, copy);
        parent.addReference(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalResourceReference<?> resourceReference, LogicalComponent parent) {
        URI uri = resourceReference.getUri();
        ResourceReferenceDefinition definition = resourceReference.getDefinition();
        LogicalResourceReference copy = new LogicalResourceReference(uri, definition, parent);
        copy.setTarget(resourceReference.getTarget());
        parent.addResource(copy);
    }


    private static void copy(LogicalService service,
                             LogicalComponent parent,
                             Map<URI, LogicalComponent<?>> components,
                             Map<URI, LogicalService> services) {
        URI uri = service.getUri();
        LogicalService copy = new LogicalService(uri, service.getDefinition(), parent);
        services.put(uri, copy);
        copy.setLeafComponent(components.get(service.getLeafComponent().getUri()));
        copy.setLeafService(services.get(service.getLeafService().getUri()));
        copy.setServiceContract(service.getServiceContract());
        copy.setPromotedUri(service.getPromotedUri());
        copy.addIntents(service.getIntents());
        copy.addPolicySets(service.getPolicySets());
        copy(service, copy);
        parent.addService(copy);
    }

    private static void copy(LogicalChannel channel, LogicalCompositeComponent parent) {
        URI uri = channel.getUri();
        LogicalChannel copy = new LogicalChannel(uri, channel.getDefinition(), parent);
        copy.setServiceContract(channel.getServiceContract());
        copy(channel, copy);
        copy.setDeployable(channel.getDeployable());
        copy.addIntents(channel.getIntents());
        copy.addPolicySets(channel.getPolicySets());
        copy.setState(channel.getState());
        copy.setZone(channel.getZone());
        parent.addChannel(copy);
    }

    private static void copy(LogicalProducer producer, LogicalComponent parent) {
        URI uri = producer.getUri();
        LogicalProducer copy = new LogicalProducer(uri, producer.getDefinition(), parent);
        copy.setServiceContract(producer.getServiceContract());
        copy.addTargets(producer.getTargets());
        copyInvocable(producer, copy);
        copy.addIntents(producer.getIntents());
        copy.addPolicySets(producer.getPolicySets());
        parent.addProducer(copy);
    }

    private static void copy(LogicalConsumer consumer, LogicalComponent parent) {
        URI uri = consumer.getUri();
        LogicalConsumer copy = new LogicalConsumer(uri, consumer.getDefinition(), parent);
        copy.setServiceContract(consumer.getServiceContract());
        copy.addSources(consumer.getSources());
        copyInvocable(consumer, copy);
        copy.addIntents(consumer.getIntents());
        copy.addPolicySets(consumer.getPolicySets());
        parent.addConsumer(copy);
    }

    @SuppressWarnings({"unchecked"})
    private static void copy(LogicalResource resource, LogicalCompositeComponent parent) {
        LogicalResource<?> copy = new LogicalResource(resource.getDefinition(), parent);
        copy.setDeployable(resource.getDeployable());
        copy.setState(resource.getState());
        copy.setZone(resource.getZone());
        parent.addResource(copy);
    }


    @SuppressWarnings({"unchecked"})
    private static void copy(Bindable from, Bindable to) {
        for (LogicalBinding<?> binding : from.getBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to, binding.getDeployable());
            copy.setState(binding.getState());
            to.addBinding(copy);
            copy.setAssigned(binding.isAssigned());
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
        for (LogicalBinding<?> binding : from.getCallbackBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to, binding.getDeployable());
            copy.setState(binding.getState());
            to.addCallbackBinding(copy);
            copy.setAssigned(binding.isAssigned());
            copy.addIntents(binding.getIntents());
            copy.addPolicySets(binding.getPolicySets());
        }
        copyInvocable(from, to);
    }

    @SuppressWarnings({"unchecked"})
    private static void copyInvocable(LogicalInvocable from, LogicalInvocable to) {
        List<LogicalOperation> operations = new ArrayList<>();
        for (LogicalOperation operation : from.getOperations()) {
            LogicalOperation copy = new LogicalOperation(operation.getDefinition(), to);
            copy.addIntents(operation.getIntents());
            copy.addPolicySets(operation.getPolicySets());
            operations.add(copy);
        }
        to.overrideOperations(operations);
    }

    private static void copyWires(LogicalComponent<?> fromComponent, Map<URI, LogicalComponent<?>> components, Map<URI, LogicalService> services) {
        LogicalComponent<?> toComponent = components.get(fromComponent.getUri());
        if (fromComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) fromComponent;
            for (LogicalComponent component : composite.getComponents()) {
                copyWires(component, components, services);
            }
        }
        for (LogicalReference fromReference : fromComponent.getReferences()) {
            LogicalReference toReference = toComponent.getReference(fromReference.getUri().getFragment());
            LogicalCompositeComponent originalParent = fromComponent.getParent();
            LogicalCompositeComponent newParent = toComponent.getParent();
            copyWires(fromReference, toReference, originalParent, newParent, services);
        }
    }

    private static void copyWires(LogicalReference fromReference,
                                  LogicalReference toReference,
                                  LogicalCompositeComponent from,
                                  LogicalCompositeComponent to,
                                  Map<URI, LogicalService> services) {
        for (LogicalWire wire : from.getWires(fromReference)) {
            QName deployable = wire.getTargetDeployable();
            boolean replaceable = wire.isReplaceable();
            LogicalService fromTarget = wire.getTarget();
            LogicalService toTarget = services.get(fromTarget.getUri());
            LogicalWire wireCopy = new LogicalWire(to, toReference, toTarget, deployable, replaceable);
            wireCopy.setState(wire.getState());
            wireCopy.setReplaces(wire.isReplaces());

            LogicalBinding fromSourceBinding = wire.getSourceBinding();
            LogicalBinding toSourceBinding = null;
            if (fromSourceBinding != null) {
                for (LogicalBinding<?> binding : toReference.getBindings()) {
                    if (fromSourceBinding.getDefinition().getName().equals(binding.getDefinition().getName())) {
                        toSourceBinding = binding;
                        break;
                    }
                }
            }
            wireCopy.setSourceBinding(toSourceBinding);

            LogicalBinding fromTargetBinding = wire.getTargetBinding();
            LogicalBinding toTargetBinding = null;
            if (fromTargetBinding != null) {
                if (!toTarget.getBindings().isEmpty()) {
                    for (LogicalBinding<?> binding : toTarget.getBindings()) {
                        if (fromTargetBinding.getDefinition().getName().equals(binding.getDefinition().getName())) {
                            toTargetBinding = binding;
                            break;
                        }
                    }
                } else {
                    for (LogicalBinding<?> binding : toTarget.getLeafService().getBindings()) {
                        if (fromTargetBinding.getDefinition().getName().equals(binding.getDefinition().getName())) {
                            toTargetBinding = binding;
                            break;
                        }
                    }
                }
            }
            wireCopy.setTargetBinding(toTargetBinding);
            to.addWire(toReference, wireCopy);
        }
    }

}

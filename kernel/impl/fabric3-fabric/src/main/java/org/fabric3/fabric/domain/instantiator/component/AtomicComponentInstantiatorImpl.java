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
package org.fabric3.fabric.domain.instantiator.component;

import java.net.URI;

import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 *
 */
public class AtomicComponentInstantiatorImpl extends AbstractComponentInstantiator implements AtomicComponentInstantiator {

    @SuppressWarnings({"unchecked"})
    public LogicalComponent instantiate(Component<?> component, LogicalCompositeComponent parent, InstantiationContext context) {
        URI uri = URI.create(parent.getUri() + "/" + component.getName());
        LogicalComponent<?> logicalComponent = new LogicalComponent(uri, component, parent);
        if (parent.getComponent(uri) != null) {
            DuplicateComponent error = new DuplicateComponent(uri, parent);
            context.addError(error);
        }
        parent.addComponent(logicalComponent);

        ComponentType componentType = component.getComponentType();
        if (componentType == null) {
            return logicalComponent;
        }
        initializeProperties(logicalComponent, component, context);
        createServices(component, logicalComponent, componentType);
        createReferences(component, logicalComponent, componentType);
        createProducers(component, logicalComponent, componentType);
        createConsumers(component, logicalComponent, componentType);
        createResourceReferences(logicalComponent, componentType);
        return logicalComponent;
    }

    private void createServices(Component<?> component, LogicalComponent<?> logicalComponent, ComponentType componentType) {
        for (Service<ComponentType> service : componentType.getServices().values()) {
            String name = service.getName();
            URI serviceUri = logicalComponent.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, logicalComponent);

            for (Binding binding : service.getBindings()) {
                LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalService);
                logicalService.addBinding(logicalBinding);
            }

            for (Binding binding : service.getCallbackBindings()) {
                LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalService);
                logicalService.addCallbackBinding(logicalBinding);
            }

            // service is configured in the component definition
            Service<Component> componentService = component.getServices().get(name);
            if (componentService != null) {
                for (Binding binding : componentService.getBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalService);
                    logicalService.addBinding(logicalBinding);
                }
                for (Binding binding : componentService.getCallbackBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalService);
                    logicalService.addCallbackBinding(logicalBinding);
                }
            }
            logicalComponent.addService(logicalService);
        }
    }

    private void createReferences(Component<?> component, LogicalComponent<?> logicalComponent, ComponentType componentType) {
        for (Reference<ComponentType> reference : componentType.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = logicalComponent.getUri().resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, logicalComponent);

            Reference<Component> componentReference = component.getReferences().get(name);
            if (componentReference != null) {
                // reference is configured in the component definition
                for (Binding binding : componentReference.getBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                    logicalReference.addBinding(logicalBinding);
                }
                for (Binding binding : componentReference.getCallbackBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                    logicalReference.addCallbackBinding(logicalBinding);
                }
            } else {
                // check if reference is configured with bindings in the component type
                for (Binding binding : reference.getBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                    logicalReference.addBinding(logicalBinding);
                }
                for (Binding binding : reference.getCallbackBindings()) {
                    LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                    logicalReference.addCallbackBinding(logicalBinding);
                }
            }
            logicalComponent.addReference(logicalReference);
        }
    }

    private void createConsumers(Component<?> definition, LogicalComponent<?> logicalComponent, ComponentType componentType) {
        for (Consumer<ComponentType> consumer : componentType.getConsumers().values()) {
            String name = consumer.getName();
            URI consumerUri = logicalComponent.getUri().resolve('#' + name);
            LogicalConsumer logicalConsumer = new LogicalConsumer(consumerUri, consumer, logicalComponent);

            // producer is configured in the logicalComponent definition
            Consumer<Component> componentConsumer = definition.getConsumers().get(name);
            if (componentConsumer != null) {
                for (URI uri : componentConsumer.getSources()) {
                    addSource(logicalConsumer, uri, logicalComponent);
                }
            } else {
                for (URI uri : consumer.getSources()) {
                    addSource(logicalConsumer, uri, logicalComponent);
                }
            }
            logicalComponent.addConsumer(logicalConsumer);
        }
    }

    private void createProducers(Component<?> component, LogicalComponent<?> logicalComponent, ComponentType componentType) {
        for (Producer<ComponentType> producer : componentType.getProducers().values()) {
            String name = producer.getName();
            URI producerUri = logicalComponent.getUri().resolve('#' + name);
            LogicalProducer logicalProducer = new LogicalProducer(producerUri, producer, logicalComponent);

            // producer is configured in the logicalComponent definition
            Producer<Component> componentProducer = component.getProducers().get(name);
            if (componentProducer != null) {
                for (URI uri : componentProducer.getTargets()) {
                    addTarget(logicalProducer, uri, logicalComponent);
                }
            } else {
                for (URI uri : producer.getTargets()) {
                    addTarget(logicalProducer, uri, logicalComponent);
                }
            }
            logicalComponent.addProducer(logicalProducer);
        }
    }

    private void addSource(LogicalConsumer logicalConsumer, URI uri, LogicalComponent<?> logicalComponent) {
        if (uri.isAbsolute()) {
            LogicalComponent<?> domain = logicalComponent.getParent();
            while (domain.getParent() != null) {
                domain = domain.getParent();
            }
            logicalConsumer.addSource(URI.create(domain.getUri().toString() + "/" + uri.getAuthority()));
        } else {
            logicalConsumer.addSource(URI.create(logicalComponent.getParent().getUri().toString() + "/" + uri.toString()));
        }
    }

    private void addTarget(LogicalProducer logicalProducer, URI uri, LogicalComponent<?> logicalComponent) {
        if (uri.isAbsolute()) {
            LogicalComponent<?> domain = logicalComponent.getParent();
            while (domain.getParent() != null) {
                domain = domain.getParent();
            }
            logicalProducer.addTarget(URI.create(domain.getUri().toString() + "/" + uri.getAuthority()));
        } else {
            logicalProducer.addTarget(URI.create(logicalComponent.getParent().getUri().toString() + "/" + uri.toString()));
        }
    }

    private void createResourceReferences(LogicalComponent<?> logicalComponent, ComponentType componentType) {
        for (ResourceReference reference : componentType.getResourceReferences().values()) {
            URI resourceUri = logicalComponent.getUri().resolve('#' + reference.getName());
            LogicalResourceReference<ResourceReference> logicalReference = new LogicalResourceReference<>(resourceUri, reference, logicalComponent);
            logicalComponent.addResource(logicalReference);
        }
    }

}

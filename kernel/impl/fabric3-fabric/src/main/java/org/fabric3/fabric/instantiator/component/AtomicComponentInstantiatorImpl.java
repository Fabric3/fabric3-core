/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.oasisopen.sca.annotation.Property;

import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.model.type.component.AbstractReference;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentConsumer;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentProducer;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.ConsumerDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ProducerDefinition;
import org.fabric3.model.type.component.ResourceReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
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
    private boolean componentTypeOverride;

    @Property(required = false)
    public void setComponentTypeOverride(boolean componentTypeOverride) {
        this.componentTypeOverride = componentTypeOverride;
    }

    @SuppressWarnings({"unchecked"})
    public LogicalComponent instantiate(ComponentDefinition<?> definition, LogicalCompositeComponent parent, InstantiationContext context) {
        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        LogicalComponent<?> component = new LogicalComponent(uri, definition, parent);
        if (parent.getComponent(uri) != null) {
            DuplicateComponent error = new DuplicateComponent(uri, parent);
            context.addError(error);
        }
        parent.addComponent(component);

        Implementation<?> impl = definition.getImplementation();
        if (impl == null) {
            return component;
        }
        ComponentType componentType = impl.getComponentType();
        if (componentTypeOverride) {
            // SCA policy conformance: override policy sets configured on the component type
            component.getPolicySets().removeAll(definition.getPolicySets());
        }
        initializeProperties(component, definition, context);
        createServices(definition, component, componentType);
        createReferences(definition, component, componentType);
        createProducers(definition, component, componentType);
        createConsumers(definition, component, componentType);
        createResourceReferences(component, componentType);
        return component;
    }

    private void createServices(ComponentDefinition<?> definition, LogicalComponent<?> component, ComponentType componentType) {
        for (ServiceDefinition service : componentType.getServices().values()) {
            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);

            for (BindingDefinition binding : service.getBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addBinding(logicalBinding);
            }

            for (BindingDefinition binding : service.getCallbackBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addCallbackBinding(logicalBinding);
            }

            // service is configured in the component definition
            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                logicalService.addIntents(componentService.getIntents());
                for (BindingDefinition binding : componentService.getBindings()) {
                    LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                    logicalService.addBinding(logicalBinding);
                }
                for (BindingDefinition binding : componentService.getCallbackBindings()) {
                    LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                    logicalService.addCallbackBinding(logicalBinding);
                }
            }
            component.addService(logicalService);
        }
    }

    private void createReferences(ComponentDefinition<?> definition, LogicalComponent<?> component, ComponentType componentType) {
        for (AbstractReference reference : componentType.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = component.getUri().resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);

            // reference is configured in the component definition
            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                logicalReference.addIntents(componentReference.getIntents());
                for (BindingDefinition binding : componentReference.getBindings()) {
                    LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalReference);
                    logicalReference.addBinding(logicalBinding);
                }
                for (BindingDefinition binding : componentReference.getCallbackBindings()) {
                    LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalReference);
                    logicalReference.addCallbackBinding(logicalBinding);
                }
            }
            component.addReference(logicalReference);
        }
    }

    private void createConsumers(ComponentDefinition<?> definition, LogicalComponent<?> component, ComponentType componentType) {
        for (ConsumerDefinition consumer : componentType.getConsumers().values()) {
            String name = consumer.getName();
            URI consumerUri = component.getUri().resolve('#' + name);
            LogicalConsumer logicalConsumer = new LogicalConsumer(consumerUri, consumer, component);

            // producer is configured in the component definition
            ComponentConsumer componentConsumer = definition.getConsumers().get(name);
            if (componentConsumer != null) {
                logicalConsumer.addIntents(componentConsumer.getIntents());
                // TODO refactor this: URIs should be resolved to channels by a separate service that also handles promotion
                for (URI uri : componentConsumer.getSources()) {
                    logicalConsumer.addSource(URI.create(component.getParent().getUri().toString() + "/" + uri.toString()));
                }
            }
            component.addConsumer(logicalConsumer);
        }
    }

    private void createProducers(ComponentDefinition<?> definition, LogicalComponent<?> component, ComponentType componentType) {
        for (ProducerDefinition producer : componentType.getProducers().values()) {
            String name = producer.getName();
            URI producerUri = component.getUri().resolve('#' + name);
            LogicalProducer logicalProducer = new LogicalProducer(producerUri, producer, component);

            // producer is configured in the component definition
            ComponentProducer componentProducer = definition.getProducers().get(name);
            if (componentProducer != null) {
                logicalProducer.addIntents(componentProducer.getIntents());
                // TODO refactor this: URIs should be resolved to channels by a separate service that also handles promotion
                for (URI uri : componentProducer.getTargets()) {
                    logicalProducer.addTarget(URI.create(component.getParent().getUri().toString() + "/" + uri.toString()));
                }
            }
            component.addProducer(logicalProducer);
        }
    }

    private void createResourceReferences(LogicalComponent<?> component, ComponentType componentType) {
        for (ResourceReferenceDefinition resourceReference : componentType.getResourceReferences().values()) {
            URI resourceUri = component.getUri().resolve('#' + resourceReference.getName());
            LogicalResourceReference<ResourceReferenceDefinition> logicalResourceReference =
                    new LogicalResourceReference<ResourceReferenceDefinition>(resourceUri, resourceReference, component);
            component.addResource(logicalResourceReference);
        }
    }

}

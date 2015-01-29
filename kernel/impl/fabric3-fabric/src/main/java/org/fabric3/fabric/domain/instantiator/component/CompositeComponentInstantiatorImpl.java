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
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentReference;
import org.fabric3.api.model.type.component.ComponentService;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.CompositeImplementation;
import org.fabric3.api.model.type.component.CompositeReference;
import org.fabric3.api.model.type.component.CompositeService;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.fabric.domain.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.ChannelInstantiator;
import org.fabric3.fabric.domain.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.WireInstantiator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates a composite component in the logical representation of a domain. Child components will be recursively instantiated if they exist.
 * <p/>
 * Service and reference information configured as part of a <code>&lt;component&gt;</code> entry and component type will be merged into a single logical
 * artifact.
 */
public class CompositeComponentInstantiatorImpl extends AbstractComponentInstantiator implements CompositeComponentInstantiator {

    private AtomicComponentInstantiator atomicInstantiator;
    private WireInstantiator wireInstantiator;
    private ChannelInstantiator channelInstantiator;
    private boolean componentTypeOverride;

    @Property(required = false)
    @Source("$systemConfig//f3:sca/@componentTypeOverride")
    public void setComponentTypeOverride(boolean componentTypeOverride) {
        this.componentTypeOverride = componentTypeOverride;
    }

    @Constructor
    public CompositeComponentInstantiatorImpl(@Reference AtomicComponentInstantiator atomicInstantiator,
                                              @Reference WireInstantiator wireInstantiator,
                                              @Reference ChannelInstantiator channelInstantiator) {
        this.atomicInstantiator = atomicInstantiator;
        this.wireInstantiator = wireInstantiator;
        this.channelInstantiator = channelInstantiator;
    }

    public CompositeComponentInstantiatorImpl(AtomicComponentInstantiator atomicInstantiator, WireInstantiator wireInstantiator) {
        this(atomicInstantiator, wireInstantiator, null);
    }

    public LogicalComponent<CompositeImplementation> instantiate(ComponentDefinition<CompositeImplementation> definition,
                                                                 LogicalCompositeComponent parent,
                                                                 InstantiationContext context) {

        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        Composite composite = definition.getImplementation().getComponentType();

        LogicalCompositeComponent component = new LogicalCompositeComponent(uri, definition, parent);
        initializeProperties(component, definition, context);
        instantiateChildComponents(component, composite, context);
        instantiateCompositeServices(component, composite);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        instantiateCompositeReferences(component, composite);
        instantiateResources(component, composite);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        if (channelInstantiator != null) {
            channelInstantiator.instantiateChannels(composite, component, context);
        }
        if (parent.getComponent(uri) != null) {
            DuplicateComponent error = new DuplicateComponent(uri, parent);
            context.addError(error);
        }
        parent.addComponent(component);
        return component;
    }

    @SuppressWarnings({"unchecked"})
    private void instantiateChildComponents(LogicalCompositeComponent component, Composite composite, InstantiationContext context) {

        // create the child components
        List<LogicalComponent<?>> children = new ArrayList<>();
        for (ComponentDefinition<? extends Implementation<?>> child : composite.getComponents().values()) {

            LogicalComponent<?> childComponent;
            if (child.getImplementation() instanceof CompositeImplementation) {
                childComponent = instantiate((ComponentDefinition<CompositeImplementation>) child, component, context);
            } else {
                childComponent = atomicInstantiator.instantiate(child, component, context);
            }
            component.addComponent(childComponent);
            children.add(childComponent);
        }
        // resolve the reference wires after the children have been instantiated and added to the parent, otherwise targets will not resolve
        for (LogicalComponent<?> child : children) {
            wireInstantiator.instantiateReferenceWires(child, context);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void instantiateCompositeServices(LogicalCompositeComponent component, Composite composite) {
        ComponentDefinition<CompositeImplementation> definition = component.getDefinition();
        String uriBase = component.getUri().toString() + "/";

        for (CompositeService service : composite.getCompositeServices().values()) {
            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            logicalService.setPromotedUri(URI.create(uriBase + service.getPromote()));

            List<BindingDefinition> serviceBindings = service.getBindings();
            for (BindingDefinition binding : serviceBindings) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalService);
                logicalService.addBinding(logicalBinding);
            }

            List<BindingDefinition> serviceCallbackBindings = service.getCallbackBindings();
            for (BindingDefinition binding : serviceCallbackBindings) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalService);
                logicalService.addCallbackBinding(logicalBinding);
            }

            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                // Merge/override logical reference configuration created above with service configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // service configuration. This information must be merged with or used to override any
                // configuration that was created by service promotions within the composite
                if (!componentService.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<>();
                    for (BindingDefinition binding : componentService.getBindings()) {
                        LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalService);
                        bindings.add(logicalBinding);
                    }
                    logicalService.overrideBindings(bindings);

                    List<LogicalBinding<?>> callbackBindings = new ArrayList<>();
                    for (BindingDefinition callbackBinding : componentService.getCallbackBindings()) {
                        LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(callbackBinding, logicalService);
                        callbackBindings.add(logicalBinding);
                    }
                    logicalService.overrideCallbackBindings(callbackBindings);

                }
            }
            component.addService(logicalService);
        }
    }

    private void instantiateCompositeReferences(LogicalCompositeComponent component, Composite composite) {
        ComponentDefinition<CompositeImplementation> definition = component.getDefinition();
        String uriBase = component.getUri().toString() + "/";

        // create logical references based on promoted references in the composite definition
        for (CompositeReference reference : composite.getCompositeReferences().values()) {
            String name = reference.getName();
            URI referenceUri = component.getUri().resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);

            List<BindingDefinition> referenceBindings = reference.getBindings();
            for (BindingDefinition binding : referenceBindings) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                logicalReference.addBinding(logicalBinding);
            }

            List<BindingDefinition> callbackBindings = reference.getCallbackBindings();
            for (BindingDefinition binding : callbackBindings) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                logicalReference.addCallbackBinding(logicalBinding);
            }

            for (URI promotedUri : reference.getPromotedUris()) {
                URI resolvedUri = URI.create(uriBase + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }

            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                // Merge/override logical reference configuration created above with reference configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // reference configuration. This information must be merged with or used to override any
                // configuration that was created by reference promotions within the composite
                if (!componentReference.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<>();
                    List<BindingDefinition> overrideBindings = componentReference.getBindings();
                    for (BindingDefinition binding : overrideBindings) {
                        LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<>(binding, logicalReference);
                        bindings.add(logicalBinding);
                    }
                    logicalReference.overrideBindings(bindings);
                }
            }
            component.addReference(logicalReference);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void instantiateResources(LogicalCompositeComponent component, Composite composite) {
        for (ResourceDefinition definition : composite.getResources()) {
            LogicalResource<?> resource = new LogicalResource(definition, component);
            component.addResource(resource);
        }
    }

}

/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
import java.util.ArrayList;
import java.util.List;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.instantiator.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.ChannelInstantiator;
import org.fabric3.fabric.instantiator.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Instantiates a composite component in the logical representation of a domain. Child components will be recursively instantiated if they exist.
 * <p/>
 * Service and reference information configured as part of a <code>&lt;component&gt;</code> entry and component type will be merged into a single
 * logical artifact.
 *
 * @version $Rev$ $Date$
 */
public class CompositeComponentInstantiatorImpl extends AbstractComponentInstantiator implements CompositeComponentInstantiator {

    private AtomicComponentInstantiator atomicInstantiator;
    private WireInstantiator wireInstantiator;
    private ChannelInstantiator channelInstantiator;
    private boolean componentTypeOverride;

    @Property(required = false)
    public void setComponentTypeOverride(boolean componentTypeOverride) {
        this.componentTypeOverride = componentTypeOverride;
    }

    public CompositeComponentInstantiatorImpl(@Reference AtomicComponentInstantiator atomicInstantiator,
                                              @Reference WireInstantiator wireInstantiator,
                                              @Reference ChannelInstantiator channelInstantiator) {
        this.atomicInstantiator = atomicInstantiator;
        this.wireInstantiator = wireInstantiator;
        this.channelInstantiator = channelInstantiator;
    }

    public LogicalComponent<CompositeImplementation> instantiate(ComponentDefinition<CompositeImplementation> definition,
                                                                 LogicalCompositeComponent parent,
                                                                 InstantiationContext context) {

        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        Composite composite = definition.getImplementation().getComponentType();

        LogicalCompositeComponent component = new LogicalCompositeComponent(uri, definition, parent);
        if (componentTypeOverride) {
            // SCA policy conformance: override policy sets configured on the component type
            component.getPolicySets().removeAll(definition.getPolicySets());
        }
        initializeProperties(component, definition, context);
        instantiateChildComponents(component, composite, context);
        instantiateCompositeServices(component, composite);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        instantiateCompositeReferences(component, composite);
        instantiateResources(component, composite);
        wireInstantiator.instantiateCompositeWires(composite, component, context);
        channelInstantiator.instantiateChannels(composite, component, context);
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
        List<LogicalComponent<?>> children = new ArrayList<LogicalComponent<?>>();
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

    private void instantiateCompositeServices(LogicalCompositeComponent component, Composite composite) {
        ComponentDefinition<CompositeImplementation> definition = component.getDefinition();
        String uriBase = component.getUri().toString() + "/";

        for (CompositeService service : composite.getCompositeServices().values()) {
            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            logicalService.setPromotedUri(URI.create(uriBase + service.getPromote()));

            for (BindingDefinition binding : service.getBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addBinding(logicalBinding);
            }

            for (BindingDefinition binding : service.getCallbackBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                logicalService.addCallbackBinding(logicalBinding);
            }

            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                // Merge/override logical reference configuration created above with service configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // service configuration. This information must be merged with or used to override any
                // configuration that was created by service promotions within the composite
                if (!componentService.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentService.getBindings()) {
                        LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalService);
                        bindings.add(logicalBinding);
                    }
                    logicalService.overrideBindings(bindings);
                }
                logicalService.addIntents(componentService.getIntents());
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

            for (BindingDefinition binding : reference.getBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalReference);
                logicalReference.addBinding(logicalBinding);
            }

            for (BindingDefinition binding : reference.getCallbackBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalReference);
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
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentReference.getBindings()) {
                        LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, logicalReference);
                        bindings.add(logicalBinding);
                    }
                    logicalReference.overrideBindings(bindings);
                }
                logicalReference.addIntents(componentReference.getIntents());
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

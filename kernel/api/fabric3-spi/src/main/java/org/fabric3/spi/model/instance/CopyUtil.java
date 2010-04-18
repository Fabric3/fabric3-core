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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.ResourceDefinition;

/**
 * Makes copies of a logical model graph.
 *
 * @version $Rev$ $Date$
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
        // Create maps to dereference pointers to components, reference and services. Since the copy is performed depth-first, the maps
        // will always be populated before a component, reference, or service needs to be dereferenced. 
        Map<URI, LogicalComponent<?>> components = new HashMap<URI, LogicalComponent<?>>();
        Map<URI, LogicalReference> references = new HashMap<URI, LogicalReference>();
        Map<URI, LogicalService> services = new HashMap<URI, LogicalService>();
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
        for (LogicalResource<?> resource : composite.getResources()) {
            copy(resource, copy);
        }
        for (LogicalService service : composite.getServices()) {
            copy(service, copy, components, services);
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
            for (LogicalResource<?> resource : component.getResources()) {
                copy(resource, copy);
            }
            for (LogicalService service : component.getServices()) {
                copy(service, copy, components, services);
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
    private static void copy(LogicalResource<?> resource, LogicalComponent parent) {
        URI uri = resource.getUri();
        ResourceDefinition definition = resource.getResourceDefinition();
        LogicalResource copy = new LogicalResource(uri, definition, parent);
        copy.setTarget(resource.getTarget());
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

    @SuppressWarnings({"unchecked"})
    private static void copy(Bindable from, Bindable to) {
        for (LogicalBinding<?> binding : from.getBindings()) {
            LogicalBinding<?> copy = new LogicalBinding(binding.getDefinition(), to, binding.getDeployable());
            copy.setState(binding.getState());
            to.addBinding(copy);
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
        List<LogicalOperation> operations = new ArrayList<LogicalOperation>();
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
                for (LogicalBinding<?> binding : toTarget.getBindings()) {
                    if (fromTargetBinding.getDefinition().getName().equals(binding.getDefinition().getName())) {
                        toTargetBinding = binding;
                        break;
                    }
                }
            }
            wireCopy.setTargetBinding(toTargetBinding);
            to.addWire(toReference, wireCopy);
        }
    }

}

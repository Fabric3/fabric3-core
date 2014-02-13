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
package org.fabric3.fabric.deployment.instantiator.wire;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentReference;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.component.WireDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.deployment.instantiator.AmbiguousService;
import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.fabric.deployment.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.deployment.instantiator.ServiceNotFound;
import org.fabric3.fabric.deployment.instantiator.WireInstantiator;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.binding.SCABinding;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the WireInstantiator.
 */
public class WireInstantiatorImpl implements WireInstantiator {
    private ContractMatcher matcher;

    public WireInstantiatorImpl(@Reference ContractMatcher matcher) {
        this.matcher = matcher;
    }

    public void instantiateCompositeWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            Target referenceTarget = definition.getReferenceTarget();
            LogicalReference reference = resolveReference(referenceTarget, parent, context);
            if (reference == null) {
                // error resolving, continue processing other targets so all errors are collated
                continue;
            }

            // resolve the target service
            Target serviceTarget = definition.getServiceTarget();
            LogicalService service = resolveService(reference, serviceTarget, parent, context);
            if (service == null) {
                // error resolving, continue processing other targets so all errors are collated
                continue;
            }

            // create the wire
            QName deployable = parent.getDeployable();
            LogicalWire wire = new LogicalWire(parent, reference, service, deployable);
            wire.setReplaces(definition.isReplace());
            String referenceBindingName = referenceTarget.getBinding();
            String serviceBindingName = serviceTarget.getBinding();
            resolveBindings(reference, referenceBindingName, service, wire, serviceBindingName, context);
            parent.addWire(reference, wire);
        }
    }

    public void instantiateReferenceWires(LogicalComponent<?> component, InstantiationContext context) {
        for (LogicalReference logicalReference : component.getReferences()) {
            instantiateReferenceWires(logicalReference, context);
        }
    }

    private void instantiateReferenceWires(LogicalReference reference, InstantiationContext context) {
        LogicalCompositeComponent parent = reference.getParent().getParent();
        ComponentReference componentReference = reference.getComponentReference();
        AbstractReference<?> definition = reference.getDefinition();
        if (componentReference == null && definition.getTargets().isEmpty()) {
            // the reference is not configured on the component definition in the composite or in the component type so there are no wires
            return;
        }

        List<Target> serviceTargets = componentReference != null ? componentReference.getTargets(): definition.getTargets();
        if (serviceTargets.isEmpty()) {
            serviceTargets = definition.getTargets();
        }
        List<SCABinding> scaBindings = new ArrayList<>();
        List<BindingDefinition> bindings = componentReference != null ? componentReference.getBindings() : definition.getBindings();
        for (BindingDefinition binding : bindings) {
            if (binding instanceof SCABinding) {
                SCABinding scaBinding = (SCABinding) binding;
                scaBindings.add(scaBinding);
            }
        }
        if (scaBindings.isEmpty()) {
            //  if the component reference has no bindings, use the composite definition's

            for (BindingDefinition binding : definition.getBindings()) {
                if (binding instanceof SCABinding) {
                    SCABinding scaBinding = (SCABinding) binding;
                    scaBindings.add(scaBinding);
                }
            }
        }
        if (serviceTargets.isEmpty() && scaBindings.isEmpty()) {
            // no targets are specified
            return;
        }

        // Check if any composite level wires with @replace=true exist. If so, ignore wires specified using the @target attribute on the reference
        // since they are overridden by the existing composite level wires.
        List<LogicalWire> existingWires = reference.getWires();
        for (LogicalWire wire : existingWires) {
            if (wire.isReplaces()) {
                reference.setResolved(true);
                return;
            }
        }

        List<LogicalWire> wires = new ArrayList<>();
        if (!scaBindings.isEmpty()) {
            // resolve the reference targets and create logical wires
            for (SCABinding binding : scaBindings) {
                Target target = binding.getTarget();
                if (target == null) {
                    // SCA binding with no target specified, don't wire
                    continue;
                }
                String bindingName = binding.getName();
                LogicalWire wire = createWire(target, reference, bindingName, parent, context);
                if (wire == null) {
                    continue;
                }
                wires.add(wire);
            }
        } else {
            // resolve the reference targets and create logical wires
            for (Target target : serviceTargets) {
                LogicalWire wire = createWire(target, reference, null, parent, context);
                if (wire == null) {
                    continue;
                }
                wires.add(wire);
            }
        }
        if (!wires.isEmpty()) {
            parent.addWires(reference, wires);
        }
        reference.setResolved(true);
    }

    /**
     * Creates a wire by resolving a Target.
     *
     * @param target      the target
     * @param reference   the source reference
     * @param bindingName the binding name or null
     * @param parent      the parent component
     * @param context     the instantiation context
     * @return the wire or null if it could not be created
     */
    private LogicalWire createWire(Target target,
                                   LogicalReference reference,
                                   String bindingName,
                                   LogicalCompositeComponent parent,
                                   InstantiationContext context) {
        LogicalService service = resolveService(reference, target, parent, context);
        if (service == null) {
            return null;
        }
        QName deployable = service.getParent().getDeployable();
        LogicalWire wire = new LogicalWire(parent, reference, service, deployable, true);
        String serviceBindingName = target.getBinding();
        resolveBindings(reference, bindingName, service, wire, serviceBindingName, context);
        return wire;
    }

    /**
     * Resolves the wire source to a reference provided by a component in the parent composite.
     *
     * @param target  the reference target
     * @param parent  the parent composite
     * @param context the logical context to report errors against
     * @return the resolved reference or null if not found
     */
    private LogicalReference resolveReference(Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        String base = parent.getUri().toString();
        // component URI is relative to the parent composite
        URI componentUri = URI.create(base + "/" + target.getComponent());
        String referenceName = target.getBindable();

        LogicalComponent<?> source = parent.getComponent(componentUri);
        if (source == null) {
            raiseWireSourceNotFound(componentUri, parent, context);
            return null;
        }
        LogicalReference logicalReference;
        if (referenceName == null) {
            // a reference was not specified
            if (source.getReferences().size() == 0) {
                raiseWireSourceNoReference(componentUri, parent, context);
                return null;
            } else if (source.getReferences().size() != 1) {
                raiseWireSourceAmbiguousReference(componentUri, parent, context);
                return null;
            }
            // default to the only reference
            logicalReference = source.getReferences().iterator().next();
        } else {
            logicalReference = source.getReference(referenceName);
            if (logicalReference == null) {
                raiseWireSourceNotFound(componentUri, referenceName, parent, context);
                return null;
            }
        }
        return logicalReference;
    }

    /**
     * Resolves and validates the wire target to a service provided by a component in the parent composite.
     *
     * @param reference the source reference of the wire
     * @param target    the service target.
     * @param parent    the parent composite to resolve against
     * @param context   the logical context to report errors against
     * @return the resolved service
     */
    private LogicalService resolveService(LogicalReference reference, Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        URI targetComponentUri = URI.create(parent.getUri().toString() + "/" + target.getComponent());
        LogicalComponent<?> targetComponent = parent.getComponent(targetComponentUri);
        if (targetComponent == null) {
            TargetComponentNotFound error = new TargetComponentNotFound(reference, targetComponentUri);
            context.addError(error);
            return null;
        }
        String serviceName = target.getBindable();
        LogicalService targetService = null;
        if (serviceName != null) {
            targetService = targetComponent.getService(serviceName);
            if (targetService == null) {
                raiseServiceNotFound(reference, target, context);
                return null;
            }
        } else {
            for (LogicalService service : targetComponent.getServices()) {
                if (targetService != null) {
                    raiseAmbiguousService(reference, target, context);
                    return null;
                }
                targetService = service;
            }
            if (targetService == null) {
                raiseNoService(reference, target, parent, context);
                return null;
            }
        }
        validate(reference, targetService, context);
        return targetService;
    }

    /**
     * Resolves any bindings specified as part of the service and reference targets of a wire
     *
     * @param reference            the logical reference to resolve against
     * @param referenceBindingName the reference binding name. May be null.
     * @param service              the logical service to resolve against
     * @param wire                 the wire to update
     * @param serviceBindingName   the service binding name. May be null.
     * @param context              the logical context
     */
    private void resolveBindings(LogicalReference reference,
                                 String referenceBindingName,
                                 LogicalService service,
                                 LogicalWire wire,
                                 String serviceBindingName,
                                 InstantiationContext context) {
        if (serviceBindingName == null) {
            return;
        }
        LogicalBinding<?> serviceBinding = getBinding(serviceBindingName, service);
        if (serviceBinding == null) {
            raiseServiceBindingNotFound(service, serviceBindingName, context);
        }

        LogicalBinding<?> referenceBinding;
        if (referenceBindingName != null) {
            referenceBinding = getBinding(referenceBindingName, reference);
        } else if (serviceBinding != null) {
            referenceBinding = selectBinding(reference, serviceBinding);
        } else {
            // error condition
            return;
        }

        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);
        if (serviceBinding != null && referenceBinding != null
            && !referenceBinding.getDefinition().getType().equals(serviceBinding.getDefinition().getType())) {
            raiseIncompatibleBindings(reference, service, referenceBindingName, context);
        }
    }

    /**
     * Selects a binding from the given bindable by matching it against another binding
     *
     * @param bindable the bindable to select the binding from
     * @param binding  the binding to match against
     * @return the selected binding or null if no matching ones were found
     */
    private LogicalBinding<?> selectBinding(Bindable bindable, LogicalBinding binding) {
        for (LogicalBinding<?> candidate : bindable.getBindings()) {
            if (candidate.getDefinition().getType().equals(binding.getDefinition().getType())) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Returns a binding matching the name
     *
     * @param name     the binding name
     * @param bindable the bindable containing the binding
     * @return the matching binding or null if no matching one was found
     */
    private LogicalBinding<?> getBinding(String name, Bindable bindable) {
        LogicalBinding<?> selectedBinding = null;
        for (LogicalBinding<?> binding : bindable.getBindings()) {
            if (name.equals(binding.getDefinition().getName())) {
                selectedBinding = binding;
                break;
            }
        }
        return selectedBinding;
    }

    private void validate(LogicalReference reference, LogicalService service, InstantiationContext context) {
        validateKeyedReference(reference, service, context);
        validateContracts(reference, service, context);
    }

    /**
     * Validates a target key is present for keyed references.
     *
     * @param reference the reference
     * @param service   the service
     * @param context   the logical context
     */
    private void validateKeyedReference(LogicalReference reference, LogicalService service, InstantiationContext context) {
        if (!reference.getDefinition().isKeyed()) {
            return;
        }
        LogicalComponent<?> parent = service.getParent();
        if (parent.getDefinition().getKey() == null && parent.getDefinition().getComponentType().getKey() == null) {
            KeyNotFound error = new KeyNotFound(reference);
            context.addError(error);
        }
    }

    /**
     * Validates the reference and service contracts match.
     *
     * @param reference the reference
     * @param service   the service
     * @param context   the logical context
     */
    private void validateContracts(LogicalReference reference, LogicalService service, InstantiationContext context) {
        ServiceContract referenceContract = reference.getServiceContract();
        ServiceContract serviceContract = service.getServiceContract();
        MatchResult result = matcher.isAssignableFrom(referenceContract, serviceContract, true);
        if (!result.isAssignable()) {
            URI serviceUri = service.getUri();
            String message = result.getError();
            IncompatibleContracts error = new IncompatibleContracts(reference, serviceUri, message);
            context.addError(error);
        }
    }

    private void raiseWireSourceNotFound(URI componentUri, String referenceName, LogicalCompositeComponent parent, InstantiationContext context) {
        WireSourceReferenceNotFound error = new WireSourceReferenceNotFound(componentUri, referenceName, parent);
        context.addError(error);
    }

    private void raiseWireSourceAmbiguousReference(URI componentUri, LogicalCompositeComponent parent, InstantiationContext context) {
        WireSourceAmbiguousReference error = new WireSourceAmbiguousReference(componentUri, parent);
        context.addError(error);
    }

    private void raiseWireSourceNoReference(URI componentUri, LogicalCompositeComponent parent, InstantiationContext context) {
        WireSourceNoReference error = new WireSourceNoReference(componentUri, parent);
        context.addError(error);
    }

    private void raiseWireSourceNotFound(URI componentUri, LogicalCompositeComponent parent, InstantiationContext context) {
        WireSourceNotFound error = new WireSourceNotFound(componentUri, parent);
        context.addError(error);
    }

    private void raiseNoService(LogicalReference reference, Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        String componentName = target.getComponent();
        URI referenceUri = reference.getUri();
        String msg = "The reference " + referenceUri + " is wired to component " + componentName + " but the component has no services";
        NoServiceOnComponent error = new NoServiceOnComponent(msg, parent);
        context.addError(error);
    }

    private void raiseAmbiguousService(LogicalReference reference, Target target, InstantiationContext context) {
        String componentName = target.getComponent();
        URI referenceUri = reference.getUri();
        String msg = "More than one service available on component: " + componentName + ". The wire from the reference " + referenceUri
                     + " must explicitly specify a target service.";
        AmbiguousService error = new AmbiguousService(msg, reference);
        context.addError(error);
    }

    private void raiseServiceNotFound(LogicalReference reference, Target target, InstantiationContext context) {
        URI referenceUri = reference.getUri();
        String componentName = target.getComponent();
        String serviceName = target.getBindable();
        String msg = "The service " + serviceName + " wired from the reference " + referenceUri + " is not found on component " + componentName;
        ServiceNotFound error = new ServiceNotFound(msg, reference);
        context.addError(error);
    }

    private void raiseServiceBindingNotFound(LogicalService service, String name, InstantiationContext context) {
        BindingNotFound error = new BindingNotFound("The binding " + name + "  for service " + service.getUri() + " was not found", service);
        context.addError(error);
    }

    private void raiseIncompatibleBindings(LogicalReference reference, LogicalService service, String name, InstantiationContext context) {

        BindingNotFound error = new BindingNotFound(
                "The binding " + name + " for reference " + reference.getUri() + " and service " + service.getUri() + " are not compatible", reference);
        context.addError(error);
    }

}

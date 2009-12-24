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
package org.fabric3.fabric.instantiator.wire;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Target;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Default implementation of the WireInstantiator.
 *
 * @version $Rev$ $Date$
 */
public class WireInstantiatorImpl implements WireInstantiator {
    private ServiceContractResolver resolver;
    private ContractMatcher matcher;

    public WireInstantiatorImpl(@Reference ServiceContractResolver resolver, @Reference ContractMatcher matcher) {
        this.resolver = resolver;
        this.matcher = matcher;
    }

    public void instantiateCompositeWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            URI contributionUri = composite.getContributionUri();

            Target referenceTarget = definition.getReferenceTarget();
            LogicalReference reference = resolveReference(referenceTarget, parent, contributionUri, context);
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
        if (componentReference == null) {
            // the reference is not configured on the component definition in the composite so there are no wires
            return;
        }

        List<Target> serviceTargets = componentReference.getTargets();
        if (serviceTargets.isEmpty()) {
            // no targets are specified
            return;
        }

        // resolve the targets and create logical wires
        List<LogicalWire> wires = new ArrayList<LogicalWire>();
        for (Target serviceTarget : serviceTargets) {
            LogicalService service = resolveService(reference, serviceTarget, parent, context);
            if (service == null) {
                return;
            }
            QName deployable = service.getParent().getDeployable();
            LogicalWire wire = new LogicalWire(parent, reference, service, deployable);
            String serviceBindingName = serviceTarget.getBinding();
            resolveBindings(reference, null, service, wire, serviceBindingName, context);
            wires.add(wire);
        }
        parent.overrideWires(reference, wires);
        reference.setResolved(true);
    }

    /**
     * Resolves the wire source to a reference provided by a component in the parent composite.
     *
     * @param target          the reference target
     * @param parent          the parent composite
     * @param contributionUri the contribution uri
     * @param context         the logical context to report errors against  @return the resolve reference
     * @return                the resolved reference or null if not found
     */
    private LogicalReference resolveReference(Target target, LogicalCompositeComponent parent, URI contributionUri, InstantiationContext context) {
        String base = parent.getUri().toString();
        // component URI is relative to the parent composite
        URI componentUri = URI.create(base + "/" + target.getComponent());
        String referenceName = target.getBindable();

        LogicalComponent<?> source = parent.getComponent(componentUri);
        if (source == null) {
            raiseWireSourceNotFound(componentUri, parent, contributionUri, context);
            return null;
        }
        LogicalReference logicalReference;
        if (referenceName == null) {
            // a reference was not specified
            if (source.getReferences().size() == 0) {
                raiseWireSourceNoReference(componentUri, parent, contributionUri, context);
                return null;
            } else if (source.getReferences().size() != 1) {
                raiseWireSourceAmbiguousReference(componentUri, parent, contributionUri, context);
                return null;
            }
            // default to the only reference
            logicalReference = source.getReferences().iterator().next();
        } else {
            logicalReference = source.getReference(referenceName);
            if (logicalReference == null) {
                raiseWireSourceNotFound(componentUri, referenceName, parent, contributionUri, context);
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
            URI referenceUri = reference.getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            URI parentUri = parent.getUri();
            TargetComponentNotFound error = new TargetComponentNotFound(referenceUri, targetComponentUri, parentUri, contributionUri);
            context.addError(error);
            return null;
        }
        String serviceName = target.getBindable();
        LogicalService targetService = null;
        if (serviceName != null) {
            targetService = targetComponent.getService(serviceName);
            if (targetService == null) {
                raiseServiceNotFound(reference, target, parent, context);
                return null;
            }
        } else {
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (targetService != null) {
                    raiseAmbiguousService(reference, target, parent, context);
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

        if (referenceBinding == null) {
            raiseReferenceBindingNotFound(reference, referenceBindingName, context);
            return;
        }
        wire.setSourceBinding(referenceBinding);
        wire.setTargetBinding(serviceBinding);
        if (serviceBinding != null && !referenceBinding.getDefinition().getType().equals(serviceBinding.getDefinition().getType())) {
            raiseIncomaptibleBindings(reference, service, referenceBindingName, context);
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
        if (parent.getDefinition().getKey() == null) {
            URI contributionUri = parent.getDefinition().getContributionUri();
            KeyNotFound error = new KeyNotFound(reference.getUri(), parent.getUri(), contributionUri);
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
        ServiceContract referenceContract = resolver.determineContract(reference);
        ServiceContract serviceContract = resolver.determineContract(service);
        MatchResult result = matcher.isAssignableFrom(referenceContract, serviceContract, true);
        if (!result.isAssignable()) {
            URI uri = reference.getParent().getUri();
            URI referenceUri = reference.getUri();
            URI serviceUri = service.getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            String message = result.getError();
            IncompatibleContracts error = new IncompatibleContracts(referenceUri, serviceUri, uri, message, contributionUri);
            context.addError(error);
        }
    }

    private void raiseWireSourceNotFound(URI componentUri,
                                         String referenceName,
                                         LogicalCompositeComponent parent,
                                         URI contributionUri,
                                         InstantiationContext context) {
        URI uri = parent.getUri();
        WireSourceReferenceNotFound error = new WireSourceReferenceNotFound(componentUri, referenceName, uri, contributionUri);
        context.addError(error);
    }

    private void raiseWireSourceAmbiguousReference(URI componentUri,
                                                   LogicalCompositeComponent parent,
                                                   URI contributionUri,
                                                   InstantiationContext context) {
        URI uri = parent.getUri();
        WireSourceAmbiguousReference error = new WireSourceAmbiguousReference(componentUri, uri, contributionUri);
        context.addError(error);
    }

    private void raiseWireSourceNoReference(URI componentUri, LogicalCompositeComponent parent, URI contributionUri, InstantiationContext context) {
        URI uri = parent.getUri();
        WireSourceNoReference error = new WireSourceNoReference(componentUri, uri, contributionUri);
        context.addError(error);
    }

    private void raiseWireSourceNotFound(URI componentUri, LogicalCompositeComponent parent, URI contributionUri, InstantiationContext context) {
        URI uri = parent.getUri();
        WireSourceNotFound error = new WireSourceNotFound(componentUri, uri, contributionUri);
        context.addError(error);
    }

    private void raiseNoService(LogicalReference reference, Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        String componentName = target.getComponent();
        URI referenceUri = reference.getUri();
        String msg = "The reference " + referenceUri + " is wired to component " + componentName + " but the component has no services";
        URI contributionUri = reference.getParent().getDefinition().getContributionUri();
        URI parentUri = parent.getUri();
        NoServiceOnComponent error = new NoServiceOnComponent(msg, parentUri, contributionUri);
        context.addError(error);
    }

    private void raiseAmbiguousService(LogicalReference reference, Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        String componentName = target.getComponent();
        URI referenceUri = reference.getUri();
        String msg = "More than one service available on component: " + componentName + ". The wire from the reference" + referenceUri
                + " must explicitly specify a target service.";
        URI parentUri = parent.getUri();
        URI contributionUri = reference.getParent().getDefinition().getContributionUri();
        AmbiguousService error = new AmbiguousService(msg, parentUri, contributionUri);
        context.addError(error);
    }

    private void raiseServiceNotFound(LogicalReference reference, Target target, LogicalCompositeComponent parent, InstantiationContext context) {
        URI referenceUri = reference.getUri();
        String componentName = target.getComponent();
        String serviceName = target.getBindable();
        String msg = "The service " + serviceName + " wired from the reference " + referenceUri + " is not found on component " + componentName;
        URI parentUri = parent.getUri();
        URI contributionUri = reference.getParent().getDefinition().getContributionUri();
        ServiceNotFound error = new ServiceNotFound(msg, referenceUri, parentUri, contributionUri);
        context.addError(error);
    }

    private void raiseReferenceBindingNotFound(LogicalReference reference, String name, InstantiationContext context) {
        LogicalCompositeComponent parent = reference.getParent().getParent();
        URI parentUri = parent.getUri();
        URI contributionUri = reference.getParent().getDefinition().getContributionUri();
        BindingNotFound error =
                new BindingNotFound("The binding " + name + " for reference " + reference.getUri() + " was not found", parentUri, contributionUri);
        context.addError(error);
    }

    private void raiseServiceBindingNotFound(LogicalService service, String name, InstantiationContext context) {
        LogicalCompositeComponent parent = service.getParent().getParent();
        URI parentUri = parent.getUri();
        URI contributionUri = service.getParent().getDefinition().getContributionUri();
        BindingNotFound error =
                new BindingNotFound("The binding " + name + "  for service " + service.getUri() + " was not found", parentUri, contributionUri);
        context.addError(error);
    }

    private void raiseIncomaptibleBindings(LogicalReference reference,
                                           LogicalService service,
                                           String name,
                                           InstantiationContext context) {

        LogicalCompositeComponent parent = reference.getParent().getParent();
        URI parentUri = parent.getUri();
        URI contributionUri = reference.getParent().getDefinition().getContributionUri();
        BindingNotFound error = new BindingNotFound("The binding " + name + " for reference " + reference.getUri() + " and service "
                + service.getUri() + " are not compatible", parentUri, contributionUri);
        context.addError(error);
    }


}

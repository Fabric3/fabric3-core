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
import java.util.List;

import org.fabric3.fabric.deployment.instantiator.AutowireInstantiator;
import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.fabric.deployment.instantiator.ReferenceNotFound;
import org.fabric3.model.type.component.AbstractReference;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Target;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.instantiator.AutowireResolver;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.binding.SCABinding;
import org.oasisopen.sca.annotation.Reference;

/**
 * Resolves unspecified reference targets using the SCA autowire algorithm. If a target is found, a corresponding LogicalWire will be created.
 */
public class AutowireInstantiatorImpl implements AutowireInstantiator {
    private AutowireResolver resolver;

    public AutowireInstantiatorImpl(@Reference AutowireResolver resolver) {
        this.resolver = resolver;
    }

    public void instantiate(LogicalComponent<?> component, InstantiationContext context) {
        resolveReferences(component, context);
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                instantiate(child, context);
            }
        }
    }

    private void resolveReferences(LogicalComponent<?> component, InstantiationContext context) {
        LogicalCompositeComponent parent = component.getParent();
        for (LogicalReference reference : component.getReferences()) {
            boolean scaTarget = isScaTarget(reference);
            if (scaTarget || reference.isConcreteBound()) {
                // reference is targeted using binding.sca or is explicitly bound so it should not be autowired
                continue;
            }
            Multiplicity multiplicityValue = reference.getDefinition().getMultiplicity();
            boolean refMultiplicity = multiplicityValue.equals(Multiplicity.ZERO_N) || multiplicityValue.equals(Multiplicity.ONE_N);
            if (refMultiplicity || !reference.isResolved()) {
                // Only resolve references that have not been resolved or ones that are multiplicities since the latter may be reinjected.
                // Explicitly set the reference to unresolved, since if it was a multiplicity it may have been previously resolved.
                reference.setResolved(false);
                resolveReference(reference, parent, context);
            }
        }
    }

    /**
     * Returns true if the reference is targeted through the binding.sca uri attribute.
     *
     * @param reference the reference
     * @return true if the reference is targeted through the binding.sca uri attribute
     */
    private boolean isScaTarget(LogicalReference reference) {
        boolean scaTarget = false;
        for (LogicalBinding<?> binding : reference.getBindings()) {
            BindingDefinition definition = binding.getDefinition();
            if (definition instanceof SCABinding && ((SCABinding) definition).getTarget() != null) {
                scaTarget = true;
                break;
            }
        }
        return scaTarget;
    }

    private void resolveReference(LogicalReference logicalReference, LogicalCompositeComponent compositeComponent, InstantiationContext context) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();

        if (componentReference == null) {
            // The reference is not configured on the component definition in the composite. i.e. it is only present in the componentType
            if (logicalReference.isResolved()) {
                return;
            }

            ServiceContract requiredContract = logicalReference.getServiceContract();

            Autowire autowire = component.getAutowire();
            if (autowire == Autowire.ON) {
                instantiateWires(logicalReference, requiredContract, compositeComponent);
            }

        } else {
            // The reference is explicitly configured on the component definition in the composite
            List<Target> targets = componentReference.getTargets();
            if (!targets.isEmpty()) {
                return;
            }

            if (componentReference.getAutowire() == Autowire.ON || (componentReference.getAutowire() == Autowire.INHERITED
                                                                    && component.getAutowire() == Autowire.ON)) {
                AbstractReference referenceDefinition = logicalReference.getDefinition();
                ServiceContract requiredContract = referenceDefinition.getServiceContract();
                boolean resolved = instantiateWires(logicalReference, requiredContract, component.getParent());
                if (!resolved) {
                    instantiateWires(logicalReference, requiredContract, compositeComponent);
                }
            }
        }

        boolean targeted = !logicalReference.getLeafReference().getWires().isEmpty();
        if (!targeted && logicalReference.getDefinition().isRequired() && !logicalReference.isConcreteBound()) {
            String referenceUri = logicalReference.getUri().toString();
            ReferenceNotFound error = new ReferenceNotFound("Unable to resolve reference " + referenceUri, logicalReference);
            context.addError(error);
        } else if (targeted) {
            logicalReference.setResolved(true);
        }
    }

    /**
     * Attempts to resolve a reference against a composite using the autowire matching algorithm. If the reference is resolved, a LogicalWire or set of
     * LogicalWires is created.
     *
     * @param logicalReference the logical reference
     * @param contract         the contract to match against
     * @param composite        the composite to resolve against
     * @return true if the reference has been resolved.
     */
    private boolean instantiateWires(LogicalReference logicalReference, ServiceContract contract, LogicalCompositeComponent composite) {
        List<LogicalService> candidates = resolver.resolve(logicalReference, contract, composite);
        if (candidates.isEmpty()) {
            return false;
        }

        // use the leaf component reference since the reference may be a composite reference and only leaf/atomic references are generated
        LogicalReference leafReference = logicalReference.getLeafReference();
        LogicalComponent<?> parent = leafReference.getParent();
        LogicalCompositeComponent parentComposite = parent.getParent();
        List<LogicalWire> existingWires = parentComposite.getWires(leafReference);

        // create the wires
        for (LogicalService target : candidates) {
            // for autowire, the deployable of the wire is the target since the wire must be removed when the target is undeployed
            QName deployable = target.getParent().getDeployable();
            // check to see if the wire already exists, in which case do not create a duplicate
            boolean skip = false;
            for (LogicalWire existingWire : existingWires) {
                if (target.equals(existingWire.getTarget())) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                LogicalWire wire = new LogicalWire(parentComposite, leafReference, target, deployable, true);
                parentComposite.addWire(leafReference, wire);
                for (LogicalWire existingWire : existingWires) {
                    // existing wires must be marked as new so they can be reinjected 
                    if (LogicalState.PROVISIONED == existingWire.getTarget().getLeafComponent().getState()) {
                        existingWire.setState(LogicalState.NEW);
                    }
                }
            }
        }
        return true;
    }

}

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
package org.fabric3.fabric.domain.instantiator.wire;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.Target;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.fabric.domain.instantiator.AutowireInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.fabric.domain.instantiator.ReferenceNotFound;
import org.fabric3.spi.domain.instantiator.AutowireResolver;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
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
            if (reference.isConcreteBound()) {
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

    private void resolveReference(LogicalReference logicalReference, LogicalCompositeComponent compositeComponent, InstantiationContext context) {

        ReferenceDefinition<ComponentDefinition> componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();

        AbstractReference<?> referenceDefinition = logicalReference.getDefinition();
        if (componentReference == null) {
            List<Target> targets = referenceDefinition.getTargets();
            if (!targets.isEmpty()) {
                return;
            }
            // The reference is not configured on the component definition in the composite or in the component type
            if (logicalReference.isResolved()) {
                return;
            }

            ServiceContract requiredContract = logicalReference.getServiceContract();

            instantiateWires(logicalReference, requiredContract, compositeComponent);

        } else if (componentReference != null) {
            // The reference is explicitly configured on the component definition in the composite or in the component type
            List<Target> targets = componentReference.getTargets();
            if (!targets.isEmpty()) {
                return;
            }

            ServiceContract requiredContract = referenceDefinition.getServiceContract();
            boolean resolved = instantiateWires(logicalReference, requiredContract, component.getParent());
            if (!resolved) {
                instantiateWires(logicalReference, requiredContract, compositeComponent);
            }
        }

        boolean targeted = !logicalReference.getLeafReference().getWires().isEmpty();
        if (!targeted && referenceDefinition.isRequired() && !logicalReference.isConcreteBound()) {
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

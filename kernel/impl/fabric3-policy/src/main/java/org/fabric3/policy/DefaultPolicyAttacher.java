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
package org.fabric3.policy;

import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluationException;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.generator.policy.PolicyAttacher;
import org.fabric3.spi.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * @version $Rev$ $Date$
 */
public class DefaultPolicyAttacher implements PolicyAttacher {
    private PolicyEvaluator policyEvaluator;
    private PolicyRegistry policyRegistry;

    public DefaultPolicyAttacher(@Reference PolicyEvaluator policyEvaluator, @Reference PolicyRegistry policyRegistry) {
        this.policyEvaluator = policyEvaluator;
        this.policyRegistry = policyRegistry;
    }

    public void attachPolicies(LogicalComponent<?> component, boolean incremental) throws PolicyEvaluationException {
        List<PolicySet> policySets = policyRegistry.getExternalAttachmentPolicies();
        attachPolicies(policySets, component, incremental);
    }

    public void attachPolicies(List<PolicySet> policySets, LogicalComponent<?> component, boolean incremental) throws PolicyEvaluationException {
        for (PolicySet policySet : policySets) {
            List<LogicalScaArtifact<?>> results = policyEvaluator.evaluate(policySet.getAttachTo(), component);
            // attach policy sets
            for (LogicalScaArtifact<?> result : results) {
                attach(policySet.getName(), result, incremental);
            }
        }
    }

    public void detachPolicies(List<PolicySet> policySets, LogicalComponent<?> component) throws PolicyEvaluationException {
        for (PolicySet policySet : policySets) {
            List<LogicalScaArtifact<?>> results = policyEvaluator.evaluate(policySet.getAttachTo(), component);
            // attach policy sets
            for (LogicalScaArtifact<?> result : results) {
                detach(policySet.getName(), result);
            }
        }
    }

    /**
     * Performs the actual attachment on the target artifact.
     *
     * @param policySet   the PolicySet to attach
     * @param target      the target to attach to
     * @param incremental if the attachment is being performed as part of an incremental deployment. If true, the state of the target is set to NEW.
     * @throws PolicyEvaluationException if an error occurs performing the attachment
     */
    void attach(QName policySet, LogicalScaArtifact<?> target, boolean incremental) throws PolicyEvaluationException {
        if (target instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent<?>) target;
            if (component.getPolicySets().contains(policySet)) {
                return;
            }
            if (incremental && !component.getPolicySets().contains(policySet)) {
                component.addPolicySet(policySet);
                processComponent(component, policySet, incremental);
            } else if (!incremental) {
                component.addPolicySet(policySet);
            }
        } else if (target instanceof LogicalService) {
            LogicalService service = (LogicalService) target;
            // add the policy to the service but mark bindings as NEW for (re)provisioning
            if (service.getPolicySets().contains(policySet) && incremental) {
                return;
            }
            service.addPolicySet(policySet);
            processService(service, policySet, incremental);
        } else if (target instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) target;
            if (reference.getPolicySets().contains(policySet)) {
                return;
            }
            reference.addPolicySet(policySet);
            processReference(reference, policySet, incremental);

        } else if (target instanceof LogicalOperation) {
            LogicalOperation operation = (LogicalOperation) target;
            if (operation.getPolicySets().contains(policySet)) {
                return;
            }
            operation.addPolicySet(policySet);
            LogicalAttachPoint attachPoint = operation.getParent();
            if (attachPoint instanceof LogicalReference) {
                processReference((LogicalReference) attachPoint, policySet, incremental);
            } else if (attachPoint instanceof LogicalService) {
                processService((LogicalService) attachPoint, policySet, incremental);
            } else {
                throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
            }
        } else if (target instanceof LogicalBinding) {
            LogicalBinding<?> binding = (LogicalBinding<?>) target;
            if (binding.getPolicySets().contains(policySet)) {
                return;
            }
            binding.addPolicySet(policySet);
            binding.setState(LogicalState.NEW);
        } else {
            throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
        }
    }

    /**
     * Performs the actual detachment on the target artifact.
     *
     * @param policySet the PolicySet to attach
     * @param target    the target to attach to
     * @throws PolicyEvaluationException if an error occurs performing the attachment
     */
    void detach(QName policySet, LogicalScaArtifact<?> target) throws PolicyEvaluationException {
        if (target instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent<?>) target;
            if (!component.getPolicySets().contains(policySet)) {
                return;
            }
            if (component.getPolicySets().contains(policySet)) {
                component.removePolicySet(policySet);
                processDetachComponent(component, policySet, true);
            }
        } else if (target instanceof LogicalService) {
            LogicalService service = (LogicalService) target;
            // remove the policy to the service but mark bindings as NEW for (re)provisioning
            if (!service.getPolicySets().contains(policySet)) {
                return;
            }
            service.removePolicySet(policySet);
            processDetachService(service, policySet, true);
        } else if (target instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) target;
            if (!reference.getPolicySets().contains(policySet)) {
                return;
            }
            reference.removePolicySet(policySet);
            processDetachReference(reference, policySet, true);

        } else if (target instanceof LogicalOperation) {
            LogicalOperation operation = (LogicalOperation) target;
            if (!operation.getPolicySets().contains(policySet)) {
                return;
            }
            operation.removePolicySet(policySet);
            LogicalAttachPoint attachPoint = operation.getParent();
            if (attachPoint instanceof LogicalReference) {
                processDetachReference((LogicalReference) attachPoint, policySet, true);
            } else if (attachPoint instanceof LogicalService) {
                processDetachService((LogicalService) attachPoint, policySet, true);
            } else {
                throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
            }
        } else if (target instanceof LogicalBinding) {
            LogicalBinding<?> binding = (LogicalBinding<?>) target;
            if (!binding.getPolicySets().contains(policySet)) {
                return;
            }
            binding.removePolicySet(policySet);
            binding.setState(LogicalState.NEW);
        } else {
            throw new PolicyEvaluationException("Invalid policy attachment type: " + target.getClass());
        }
    }

    private void processComponent(LogicalComponent<?> component, QName policySet, boolean incremental) {
        // do not mark the component as new, just the wires since the implementation does not need to be reprovisioned
        for (LogicalReference reference : component.getReferences()) {
            processReference(reference, policySet, incremental);
        }
        for (LogicalService service : component.getServices()) {
            processService(service, policySet, incremental);
        }
    }

    private void processService(LogicalService service, QName policySet, boolean incremental) {
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (incremental && binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
        // TODO check collocated wires, i.e. references attached directly to the service so they can be reprovisioned
    }

    private void processReference(LogicalReference reference, QName policySet, boolean incremental) {
        for (LogicalWire wire : reference.getWires()) {
            wire.setState(LogicalState.NEW);
        }
        for (LogicalBinding<?> binding : reference.getBindings()) {
            if (incremental && binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
    }

    private void processDetachComponent(LogicalComponent<?> component, QName policySet, boolean incremental) {
        // do not mark the component as new, just the wires since the implementation does not need to be reprovisioned
        for (LogicalReference reference : component.getReferences()) {
            processDetachReference(reference, policySet, incremental);
        }
        for (LogicalService service : component.getServices()) {
            processDetachService(service, policySet, incremental);
        }
    }

    private void processDetachService(LogicalService service, QName policySet, boolean incremental) {
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (incremental && !binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
        // TODO check collocated wires, i.e. references attached directly to the service so they can be reprovisioned
    }

    private void processDetachReference(LogicalReference reference, QName policySet, boolean incremental) {
        for (LogicalWire wire : reference.getWires()) {
            wire.setState(LogicalState.NEW);
        }
        for (LogicalBinding<?> binding : reference.getBindings()) {
            if (incremental && !binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
    }


}
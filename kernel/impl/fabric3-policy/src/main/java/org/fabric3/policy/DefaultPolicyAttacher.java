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
package org.fabric3.policy;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.fabric3.api.model.type.definitions.ExternalAttachment;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluationException;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.domain.generator.policy.PolicyAttacher;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class DefaultPolicyAttacher implements PolicyAttacher {
    private PolicyEvaluator policyEvaluator;
    private PolicyRegistry policyRegistry;

    public DefaultPolicyAttacher(@Reference PolicyEvaluator policyEvaluator, @Reference PolicyRegistry policyRegistry) {
        this.policyEvaluator = policyEvaluator;
        this.policyRegistry = policyRegistry;
    }

    public void attachPolicies(LogicalComponent<?> component) throws PolicyEvaluationException {
        Collection<ExternalAttachment> externalAttachments = policyRegistry.getAllDefinitions(ExternalAttachment.class);
        if (!externalAttachments.isEmpty()) {

            for (ExternalAttachment externalAttachment : externalAttachments) {
                for (QName name : externalAttachment.getPolicySets()) {
                    PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
                    if (policySet == null) {
                        throw new PolicyEvaluationException("Policy set referenced in external attachment not found: " + name);
                    }
                    attachPolicy(component, policySet, externalAttachment.getAttachTo());
                }
                for (QName name : externalAttachment.getIntents()) {
                    Intent intent = policyRegistry.getDefinition(name, Intent.class);
                    if (intent == null) {
                        throw new PolicyEvaluationException("Intent referenced in external attachment not found: " + name);
                    }
                    //intentMap.put(intent, externalAttachment.getAttachTo());
                }
            }

        }
        Set<PolicySet> policySets = policyRegistry.getExternalAttachmentPolicies();
        attachPolicies(policySets, component);
    }

    public void attachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component) throws PolicyEvaluationException {
        for (PolicySet policySet : policySets) {
            attachPolicy(component, policySet, policySet.getAttachTo());
        }
    }

    public void detachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component) throws PolicyEvaluationException {
        for (PolicySet policySet : policySets) {
            Collection<LogicalScaArtifact<?>> results = policyEvaluator.evaluate(policySet.getAttachTo(), component);

            for (Iterator<LogicalScaArtifact<?>> iterator = results.iterator(); iterator.hasNext(); ) {
                LogicalScaArtifact<?> result = iterator.next();
                String appliesTo = policySet.getAppliesTo();
                if (appliesTo != null && !policyEvaluator.doesApply(appliesTo, result)) {
                    iterator.remove();
                }
            }

            // detach policy sets
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
     * @throws PolicyEvaluationException if an error occurs performing the attachment
     */
    void attach(QName policySet, LogicalScaArtifact<?> target) throws PolicyEvaluationException {
        if (target instanceof LogicalComponent) {
            LogicalComponent<?> component = (LogicalComponent<?>) target;
            if (component.getPolicySets().contains(policySet)) {
                return;
            }
            if (!component.getPolicySets().contains(policySet)) {
                component.addPolicySet(policySet);
                processComponent(component, policySet);
            } else {
                component.addPolicySet(policySet);
            }
        } else if (target instanceof LogicalService) {
            LogicalService service = (LogicalService) target;
            // add the policy to the service but mark bindings as NEW for (re)provisioning
            if (service.getPolicySets().contains(policySet)) {
                return;
            }
            service.addPolicySet(policySet);
            processService(service, policySet);
        } else if (target instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) target;
            if (reference.getPolicySets().contains(policySet)) {
                return;
            }
            reference.addPolicySet(policySet);
            processReference(reference, policySet);

        } else if (target instanceof LogicalOperation) {
            LogicalOperation operation = (LogicalOperation) target;
            if (operation.getPolicySets().contains(policySet)) {
                return;
            }
            operation.addPolicySet(policySet);
            LogicalAttachPoint attachPoint = operation.getParent();
            if (attachPoint instanceof LogicalReference) {
                processReference((LogicalReference) attachPoint, policySet);
            } else if (attachPoint instanceof LogicalService) {
                processService((LogicalService) attachPoint, policySet);
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
                processDetachComponent(component, policySet);
            }
        } else if (target instanceof LogicalService) {
            LogicalService service = (LogicalService) target;
            // remove the policy to the service but mark bindings as NEW for (re)provisioning
            if (!service.getPolicySets().contains(policySet)) {
                return;
            }
            service.removePolicySet(policySet);
            processDetachService(service, policySet);
        } else if (target instanceof LogicalReference) {
            LogicalReference reference = (LogicalReference) target;
            if (!reference.getPolicySets().contains(policySet)) {
                return;
            }
            reference.removePolicySet(policySet);
            processDetachReference(reference, policySet);

        } else if (target instanceof LogicalOperation) {
            LogicalOperation operation = (LogicalOperation) target;
            if (!operation.getPolicySets().contains(policySet)) {
                return;
            }
            operation.removePolicySet(policySet);
            LogicalAttachPoint attachPoint = operation.getParent();
            if (attachPoint instanceof LogicalReference) {
                processDetachReference((LogicalReference) attachPoint, policySet);
            } else if (attachPoint instanceof LogicalService) {
                processDetachService((LogicalService) attachPoint, policySet);
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

    private void attachPolicy(LogicalComponent<?> component, PolicySet policySet, String attachTo) throws PolicyEvaluationException {
        Collection<LogicalScaArtifact<?>> results = policyEvaluator.evaluate(attachTo, component);

        for (Iterator<LogicalScaArtifact<?>> iterator = results.iterator(); iterator.hasNext(); ) {
            LogicalScaArtifact<?> result = iterator.next();
            String appliesTo = policySet.getAppliesTo();
            if (appliesTo != null && !policyEvaluator.doesApply(appliesTo, result)) {
                iterator.remove();
            }
        }

        // attach policy sets
        for (LogicalScaArtifact<?> result : results) {
            attach(policySet.getName(), result);
        }
    }

    private void processComponent(LogicalComponent<?> component, QName policySet) {
        // do not mark the component as new, just the wires since the implementation does not need to be reprovisioned
        for (LogicalReference reference : component.getReferences()) {
            processReference(reference, policySet);
        }
        for (LogicalService service : component.getServices()) {
            processService(service, policySet);
        }
    }

    private void processService(LogicalService service, QName policySet) {
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
        // TODO check collocated wires, i.e. references attached directly to the service so they can be reprovisioned
    }

    private void processReference(LogicalReference reference, QName policySet) {
        for (LogicalWire wire : reference.getWires()) {
            wire.setState(LogicalState.NEW);
        }
        for (LogicalBinding<?> binding : reference.getBindings()) {
            if (binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
    }

    private void processDetachComponent(LogicalComponent<?> component, QName policySet) {
        // do not mark the component as new, just the wires since the implementation does not need to be reprovisioned
        for (LogicalReference reference : component.getReferences()) {
            processDetachReference(reference, policySet);
        }
        for (LogicalService service : component.getServices()) {
            processDetachService(service, policySet);
        }
    }

    private void processDetachService(LogicalService service, QName policySet) {
        for (LogicalBinding<?> binding : service.getBindings()) {
            if (!binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
        // TODO check collocated wires, i.e. references attached directly to the service so they can be reprovisioned
    }

    private void processDetachReference(LogicalReference reference, QName policySet) {
        for (LogicalWire wire : reference.getWires()) {
            wire.setState(LogicalState.NEW);
        }
        for (LogicalBinding<?> binding : reference.getBindings()) {
            if (!binding.getPolicySets().contains(policySet)) {
                continue;
            }
            binding.setState(LogicalState.NEW);
        }
    }

}
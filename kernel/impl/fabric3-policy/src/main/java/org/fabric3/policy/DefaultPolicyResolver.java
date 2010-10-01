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
package org.fabric3.policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicyPhase;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.policy.resolver.ImplementationPolicyResolver;
import org.fabric3.policy.resolver.InteractionPolicyResolver;
import org.fabric3.spi.contract.OperationNotFoundException;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.generator.policy.EffectivePolicy;
import org.fabric3.spi.generator.policy.PolicyMetadata;
import org.fabric3.spi.generator.policy.PolicyResolutionException;
import org.fabric3.spi.generator.policy.PolicyResolver;
import org.fabric3.spi.generator.policy.PolicyResult;
import org.fabric3.util.closure.Closure;
import org.fabric3.util.closure.CollectionUtils;

/**
 * @version $Rev$ $Date$
 */
public class DefaultPolicyResolver implements PolicyResolver {
    private static final QName IMPLEMENTATION_SYSTEM = new QName(Namespaces.IMPLEMENTATION, "implementation.system");
    private static final QName IMPLEMENTATION_SINGLETON = new QName(Namespaces.IMPLEMENTATION, "singleton");
    private static final PolicyResult EMPTY_RESULT = new NullPolicyResult();

    /**
     * Closure for filtering intercepted policies.
     */
    private static final Closure<PolicySet, Boolean> INTERCEPTION = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.INTERCEPTION;
        }
    };

    /**
     * Closure for filtering provided policies by bindings or implementations.
     */
    private static final Closure<PolicySet, Boolean> PROVIDED = new Closure<PolicySet, Boolean>() {
        public Boolean execute(PolicySet policySet) {
            return policySet.getPhase() == PolicyPhase.PROVIDED;
        }
    };

    private InteractionPolicyResolver interactionResolver;
    private ImplementationPolicyResolver implementationResolver;
    private OperationResolver operationResolver;

    public DefaultPolicyResolver(@Reference InteractionPolicyResolver interactionResolver,
                                 @Reference ImplementationPolicyResolver implementationResolver,
                                 @Reference OperationResolver operationResolver) {
        this.interactionResolver = interactionResolver;
        this.implementationResolver = implementationResolver;
        this.operationResolver = operationResolver;
    }

    public PolicyResult resolvePolicies(List<LogicalOperation> operations,
                                        LogicalBinding<?> sourceBinding,
                                        LogicalBinding<?> targetBinding,
                                        LogicalComponent<?> source,
                                        LogicalComponent<?> target) throws PolicyResolutionException {
        if ((noPolicy(source) && noPolicy(target))) {
            return EMPTY_RESULT;
        }
        PolicyResultImpl policyResult = new PolicyResultImpl();

        resolveEndpointPolicies(policyResult, sourceBinding, targetBinding);

        for (LogicalOperation operation : operations) {
            resolveOperationPolicies(operation, policyResult, sourceBinding, targetBinding, target);
        }
        return policyResult;
    }

    /**
     * Resolves configured source and target intents and policies for an endpoint. Resolution will be performed against the bindings and ancestors.
     *
     * @param policyResult  the policy result to populate
     * @param sourceBinding the source binding
     * @param targetBinding the target binding
     * @throws PolicyResolutionException if there is a resolution error
     */
    private void resolveEndpointPolicies(PolicyResultImpl policyResult, LogicalBinding<?> sourceBinding, LogicalBinding<?> targetBinding)
            throws PolicyResolutionException {
        Set<Intent> sourceEndpointIntents = interactionResolver.resolveProvidedIntents(sourceBinding);
        policyResult.addSourceEndpointIntents(sourceEndpointIntents);

        Set<Intent> targetEndpointIntents = interactionResolver.resolveProvidedIntents(targetBinding);
        policyResult.addTargetEndpointIntents(targetEndpointIntents);

        Set<PolicySet> endpointPolicies = interactionResolver.resolvePolicySets(sourceBinding);
        policyResult.addSourceEndpointPolicySets(CollectionUtils.filter(endpointPolicies, PROVIDED));
        policyResult.addInterceptedEndpointPolicySets(CollectionUtils.filter(endpointPolicies, INTERCEPTION));

        endpointPolicies = interactionResolver.resolvePolicySets(targetBinding);
        policyResult.addTargetEndpointPolicySets(CollectionUtils.filter(endpointPolicies, PROVIDED));
        policyResult.addInterceptedEndpointPolicySets(CollectionUtils.filter(endpointPolicies, INTERCEPTION));
    }


    /**
     * Resolves configured source and target intents and policies for an operation.
     *
     * @param operation     the operation
     * @param policyResult  the policy result to populate
     * @param sourceBinding the source binding
     * @param targetBinding the target binding
     * @param target        the target component, or null if the operation invokes a remote service
     * @throws PolicyResolutionException if there is a resolution error
     */
    private void resolveOperationPolicies(LogicalOperation operation,
                                          PolicyResultImpl policyResult,
                                          LogicalBinding<?> sourceBinding,
                                          LogicalBinding<?> targetBinding,
                                          LogicalComponent<?> target) throws PolicyResolutionException {
        Set<Intent> sourceOperationIntents = interactionResolver.resolveProvidedIntents(operation, sourceBinding);
        policyResult.addSourceIntents(operation, sourceOperationIntents);

        Set<Intent> targetOperationIntents = interactionResolver.resolveProvidedIntents(operation, targetBinding);
        policyResult.addTargetIntents(operation, targetOperationIntents);
        if (target != null) {
            Set<Intent> sourceImplementationIntents = implementationResolver.resolveProvidedIntents(target, operation);
            policyResult.addSourceIntents(operation, sourceImplementationIntents);
        }

        Set<PolicySet> policies = interactionResolver.resolvePolicySets(operation, sourceBinding);
        policyResult.addSourcePolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
        policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

        policies = interactionResolver.resolvePolicySets(operation, targetBinding);
        policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
        policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));

        if (target != null) {
            Bindable parent = targetBinding.getParent();
            // resolve policies using the target (as opposed to source) operation so target implementation policies are included
            LogicalOperation targetOperation = matchOperation(operation, parent);
            policies = implementationResolver.resolvePolicySets(target, targetOperation);
            // add policy metadata to the result
            PolicyMetadata metadata = policyResult.getMetadata(operation);
            metadata.addAll(targetOperation.getDefinition().getMetadata());
            // add metadata from implementation
            metadata.addAll(targetOperation.getParent().getParent().getDefinition().getImplementation().getMetadata());
            // important: use reference side operation as the key
            policyResult.addTargetPolicySets(operation, CollectionUtils.filter(policies, PROVIDED));
            policyResult.addInterceptedPolicySets(operation, CollectionUtils.filter(policies, INTERCEPTION));
        }
    }

    /**
     * Matches operation definitions on the source and target sides of a wire so that policy sets and intents can be determined. Note that if the
     * source operation belongs to a service, the wire is a callback wire.
     *
     * @param operation the source operation to match against.
     * @param bindable  the target bindable.
     * @return the matching operation
     * @throws PolicyResolutionException if there is a matching error
     */
    private LogicalOperation matchOperation(LogicalOperation operation, Bindable bindable) throws PolicyResolutionException {
        String name = operation.getDefinition().getName();

        List<LogicalOperation> operations;
        if (bindable instanceof LogicalReference) {
            // target is a reference so this is a callback
            operations = bindable.getCallbackOperations();
        } else {
            operations = bindable.getOperations();
        }

        try {
            LogicalOperation matched = operationResolver.resolve(operation, operations);
            if (matched == null) {
                throw new AssertionError("No matching operation for " + name);
            }
            return matched;
        } catch (OperationNotFoundException e) {
            throw new PolicyResolutionException(e);
        }
    }

    private boolean noPolicy(LogicalComponent<?> component) {
        return component != null && (component.getDefinition().getImplementation().isType(IMPLEMENTATION_SYSTEM)
                || component.getDefinition().getImplementation().isType(IMPLEMENTATION_SINGLETON));
    }

    private static class NullPolicyResult implements PolicyResult {
        private PolicyMetadata metadata = new PolicyMetadata();

        public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public EffectivePolicy getSourcePolicy() {
            return new NullEffectivePolicy();
        }

        public EffectivePolicy getTargetPolicy() {
            return new NullEffectivePolicy();
        }

        public Set<PolicySet> getInterceptedEndpointPolicySets() {
            return Collections.emptySet();
        }

        public PolicyMetadata getMetadata(LogicalOperation operation) {
            return metadata;
        }

    }

    private static class NullEffectivePolicy implements EffectivePolicy {

        public Set<Intent> getEndpointIntents() {
            return Collections.emptySet();
        }

        public Set<PolicySet> getEndpointPolicySets() {
            return Collections.emptySet();
        }

        public List<Intent> getIntents(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<PolicySet> getPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public List<Intent> getOperationIntents() {
            return Collections.emptyList();
        }

        public Map<LogicalOperation, List<PolicySet>> getOperationPolicySets() {
            return Collections.emptyMap();
        }

    }
}

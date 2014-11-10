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
package org.fabric3.policy.resolver;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.definitions.ImplementationType;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.policy.infoset.PolicyEvaluator;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ImplementationPolicyResolverImpl extends AbstractPolicyResolver implements ImplementationPolicyResolver {

    public ImplementationPolicyResolverImpl(@Reference PolicyRegistry policyRegistry,
                                            @Reference LogicalComponentManager lcm,
                                            @Reference PolicyEvaluator policyEvaluator) {
        super(policyRegistry, lcm, policyEvaluator);
    }

    public IntentPair resolveIntents(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = policyRegistry.getDefinition(type, ImplementationType.class);

        Set<QName> mayProvidedIntents;
        if (implementationType == null) {
            // tolerate not having a registered implementation type definition
            mayProvidedIntents = Collections.emptySet();
        } else {
            mayProvidedIntents = implementationType.getMayProvide();
        }

        Set<Intent> requiredIntents = getRequestedIntents(component, operation);

        Set<Intent> intentsToBeProvided = new LinkedHashSet<>();
        for (Intent intent : requiredIntents) {
            if (mayProvidedIntents.contains(intent.getName())) {
                intentsToBeProvided.add(intent);
            }
        }
        return new IntentPair(requiredIntents, intentsToBeProvided);

    }

    public Set<PolicySet> resolvePolicySets(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException {

        Implementation<?> implementation = component.getDefinition().getImplementation();
        QName type = implementation.getType();
        ImplementationType implementationType = policyRegistry.getDefinition(type, ImplementationType.class);

        Set<QName> alwaysProvidedIntents = new LinkedHashSet<>();
        Set<QName> mayProvidedIntents = new LinkedHashSet<>();

        if (implementationType != null) {
            // tolerate not having a registered implementation type definition
            alwaysProvidedIntents = implementationType.getAlwaysProvide();
            mayProvidedIntents = implementationType.getMayProvide();
        }

        Set<Intent> requiredIntents = getRequestedIntents(component, operation);
        Set<Intent> requiredIntentsCopy = new HashSet<>(requiredIntents);

        // Remove intents that are provided
        for (Intent intent : requiredIntentsCopy) {
            QName intentName = intent.getName();
            if (alwaysProvidedIntents.contains(intentName) || mayProvidedIntents.contains(intentName)) {
                requiredIntents.remove(intent);
            }
        }
        Set<QName> policySets = aggregatePolicySets(operation, component);
        if (requiredIntents.isEmpty() && policySets.isEmpty()) {
            // short-circuit intent resolution
            return Collections.emptySet();
        }
        Set<PolicySet> policies;
        if (!policySets.isEmpty()) {
            // resolve intents against specified policy sets
            policies = new LinkedHashSet<>();
            for (QName name : policySets) {
                PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
                policies.add(policySet);
            }
            for (Intent intent : requiredIntents) {
                boolean resolved = false;
                for (PolicySet policy : policies) {
                    if (policy.doesProvide(intent.getName())) {
                        resolved = true;
                        break;
                    }
                }
                if (!resolved) {
                    throw new PolicyResolutionException("Intent not satisfied: " + intent.getName());
                }
            }
        } else {
            policies = resolvePolicies(requiredIntents, component);
            if (!requiredIntents.isEmpty()) {
                throw new IntentResolutionException("Unable to resolve all intents", requiredIntents);
            }

            for (QName name : policySets) {
                PolicySet policySet = policyRegistry.getDefinition(name, PolicySet.class);
                policies.add(policySet);
            }

        }
        return policies;

    }

    private Set<Intent> getRequestedIntents(LogicalComponent<?> logicalComponent, LogicalOperation operation) throws PolicyResolutionException {

        // Aggregate all the intents from the ancestors
        Set<QName> intentNames = new LinkedHashSet<>();
        intentNames.addAll(operation.getIntents());
        ComponentDefinition<? extends Implementation<?>> definition = logicalComponent.getDefinition();
        Implementation<?> implementation = definition.getImplementation();
        ComponentType componentType = implementation.getComponentType();
        intentNames.addAll(implementation.getIntents());
        intentNames.addAll(componentType.getIntents());
        intentNames.addAll(aggregateIntents(logicalComponent));

        // Expand all the profile intents
        Set<Intent> requiredIntents = resolveIntents(intentNames);

        // Remove intents not applicable to the artifact
        filterInvalidIntents(Intent.IMPLEMENTATION, requiredIntents);
        filterMutuallyExclusiveIntents(requiredIntents);

        return requiredIntents;

    }

    /**
     * Aggregate policies from ancestors.
     *
     * @param operation        the operation
     * @param logicalComponent the target component
     * @return the aggregated policy sets
     */
    protected Set<QName> aggregatePolicySets(LogicalOperation operation, LogicalComponent<?> logicalComponent) {
        LogicalScaArtifact<?> temp = operation;
        Set<QName> policySetNames = new LinkedHashSet<>();
        while (temp != null) {
            policySetNames.addAll(temp.getPolicySets());
            temp = temp.getParent();
        }
        policySetNames.addAll(logicalComponent.getPolicySets());
        return policySetNames;
    }

}

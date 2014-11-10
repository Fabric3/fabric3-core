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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.domain.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 *
 */
public class PolicyResultImpl implements PolicyResult {

    private EffectivePolicyImpl sourcePolicy = new EffectivePolicyImpl();
    private EffectivePolicyImpl targetPolicy = new EffectivePolicyImpl();
    private Set<PolicySet> interceptedEndpointPolicySets = new HashSet<>();
    private Map<LogicalOperation, PolicyMetadata> metadataMap = new HashMap<>();
    private Map<LogicalOperation, List<PolicySet>> interceptedPolicySets = new HashMap<>();

    public EffectivePolicy getSourcePolicy() {
        return sourcePolicy;
    }

    public EffectivePolicy getTargetPolicy() {
        return targetPolicy;
    }

    public Set<PolicySet> getInterceptedEndpointPolicySets() {
        return interceptedEndpointPolicySets;
    }

    public Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets() {
        return interceptedPolicySets;
    }

    public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
        List<PolicySet> sets = interceptedPolicySets.get(operation);
        if (sets == null) {
            return Collections.emptyList();
        }
        return sets;
    }

    public PolicyMetadata getMetadata(LogicalOperation operation) {
        PolicyMetadata metadata = metadataMap.get(operation);
        if (metadata == null) {
            metadata = new PolicyMetadata();
            metadataMap.put(operation, metadata);
        }
        return metadata;
    }

    public Map<LogicalOperation, PolicyMetadata> getMetadata() {
        return metadataMap;
    }

    void addSourceAggregatedEndpointIntents(Set<Intent> intents) {
        sourcePolicy.addAggregatedEndpointIntents(intents);
    }

    void addSourceProvidedEndpointIntents(Set<Intent> intents) {
        sourcePolicy.addProvidedEndpointIntents(intents);
    }

    void addSourceProvidedIntents(LogicalOperation operation, Set<Intent> intents) {
        sourcePolicy.addProvidedIntents(operation, intents);
    }

    void addTargetAggregatedEndpointIntents(Set<Intent> intents) {
        targetPolicy.addAggregatedEndpointIntents(intents);
    }

    void addTargetProvidedEndpointIntents(Set<Intent> intents) {
        targetPolicy.addProvidedEndpointIntents(intents);
    }

    void addTargetProvidedIntents(LogicalOperation operation, Set<Intent> intents) {
        targetPolicy.addProvidedIntents(operation, intents);
    }

    void addSourceEndpointPolicySets(Set<PolicySet> policySets) {
        sourcePolicy.addEndpointPolicySets(policySets);
    }

    void addSourcePolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        sourcePolicy.addPolicySets(operation, policySets);
    }

    void addTargetEndpointPolicySets(Set<PolicySet> policySets) {
        targetPolicy.addEndpointPolicySets(policySets);
    }

    void addTargetPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        targetPolicy.addPolicySets(operation, policySets);
    }

    public void addInterceptedEndpointPolicySets(Set<PolicySet> policySets) {
        interceptedEndpointPolicySets.addAll(policySets);
    }

    void addInterceptedPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {

        if (!interceptedPolicySets.containsKey(operation)) {
            interceptedPolicySets.put(operation, new ArrayList<PolicySet>());
        }

        List<PolicySet> interceptedSets = interceptedPolicySets.get(operation);
        for (PolicySet policySet : policySets) {
            if (!interceptedSets.contains(policySet)) {
                // Check to see if the policy set has already been added. This can happen for intents specified on service contracts, as they will
                // be picked up on both the reference and service sides of a wire.
                interceptedSets.add(policySet);
            }
        }
    }

}

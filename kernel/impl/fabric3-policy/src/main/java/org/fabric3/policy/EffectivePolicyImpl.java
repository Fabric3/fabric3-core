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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 *
 */
public class EffectivePolicyImpl implements EffectivePolicy {
    private Set<Intent> providedEndpointIntents = new HashSet<>();
    private Set<Intent> aggregatedEndpointIntents = new HashSet<>();

    private Set<PolicySet> endpointPolicySets = new HashSet<>();

    private Map<LogicalOperation, List<Intent>> providedIntentMap = new HashMap<>();
    private Map<LogicalOperation, List<PolicySet>> policySetMap = new HashMap<>();

    public Set<Intent> getProvidedEndpointIntents() {
        return providedEndpointIntents;
    }

    public Set<Intent> getAggregatedEndpointIntents() {
        return aggregatedEndpointIntents;
    }

    public Set<PolicySet> getEndpointPolicySets() {
        return endpointPolicySets;
    }

    public List<Intent> getOperationIntents() {
        List<Intent> ret = new ArrayList<>();
        for (LogicalOperation operation : providedIntentMap.keySet()) {
            ret.addAll(getIntents(operation));
        }
        return ret;
    }

    public Map<LogicalOperation, List<PolicySet>> getOperationPolicySets() {
        return policySetMap;
    }

    public List<Intent> getIntents(LogicalOperation operation) {
        return providedIntentMap.get(operation);
    }

    public List<PolicySet> getPolicySets(LogicalOperation operation) {
        return policySetMap.get(operation);
    }

    void addProvidedEndpointIntents(Set<Intent> intents) {
        providedEndpointIntents.addAll(intents);
    }

    void addAggregatedEndpointIntents(Set<Intent> intents) {
        aggregatedEndpointIntents.addAll(intents);
    }

    void addEndpointPolicySets(Set<PolicySet> policySets) {
        endpointPolicySets.addAll(policySets);
    }

    void addProvidedIntents(LogicalOperation operation, Set<Intent> intents) {
        if (!providedIntentMap.containsKey(operation)) {
            providedIntentMap.put(operation, new ArrayList<Intent>());
        }
        providedIntentMap.get(operation).addAll(intents);
    }

    void addPolicySets(LogicalOperation operation, Set<PolicySet> policySets) {
        if (!policySetMap.containsKey(operation)) {
            policySetMap.put(operation, new ArrayList<PolicySet>());
        }
        policySetMap.get(operation).addAll(policySets);
    }

}

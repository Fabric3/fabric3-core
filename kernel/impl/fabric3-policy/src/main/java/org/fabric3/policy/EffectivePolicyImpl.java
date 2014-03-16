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

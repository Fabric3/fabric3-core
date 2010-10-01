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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.generator.policy.EffectivePolicy;
import org.fabric3.spi.generator.policy.PolicyMetadata;
import org.fabric3.spi.generator.policy.PolicyResult;

/**
 * @version $Rev$ $Date$
 */
public class PolicyResultImpl implements PolicyResult {

    private EffectivePolicyImpl sourcePolicy = new EffectivePolicyImpl();
    private EffectivePolicyImpl targetPolicy = new EffectivePolicyImpl();
    private Set<PolicySet> interceptedEndpointPolicySets = new HashSet<PolicySet>();
    private Map<LogicalOperation, PolicyMetadata> metadataMap = new HashMap<LogicalOperation, PolicyMetadata>();
    private Map<LogicalOperation, List<PolicySet>> interceptedPolicySets = new HashMap<LogicalOperation, List<PolicySet>>();

    public EffectivePolicy getSourcePolicy() {
        return sourcePolicy;
    }

    public EffectivePolicy getTargetPolicy() {
        return targetPolicy;
    }

    public Set<PolicySet> getInterceptedEndpointPolicySets() {
        return interceptedEndpointPolicySets;
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

    void addSourceEndpointIntents(Set<Intent> intents) {
        sourcePolicy.addEndpointIntents(intents);
    }

    void addSourceIntents(LogicalOperation operation, Set<Intent> intents) {
        sourcePolicy.addIntents(operation, intents);
    }

    void addTargetEndpointIntents(Set<Intent> intents) {
        targetPolicy.addEndpointIntents(intents);
    }

    void addTargetIntents(LogicalOperation operation, Set<Intent> intents) {
        targetPolicy.addIntents(operation, intents);
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

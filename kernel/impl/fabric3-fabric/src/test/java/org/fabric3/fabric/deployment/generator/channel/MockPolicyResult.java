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
package org.fabric3.fabric.deployment.generator.channel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.deployment.generator.policy.PolicyMetadata;
import org.fabric3.spi.deployment.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 *
 */
public class MockPolicyResult implements PolicyResult, EffectivePolicy {
    private Map<LogicalOperation, PolicyMetadata> metadata = new HashMap<>();
    private Map<LogicalOperation, List<PolicySet>> intercepted = new HashMap<>();

    public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
        return Collections.emptyList();
    }

    public Map<LogicalOperation, PolicyMetadata> getMetadata() {
        return metadata;
    }

    public EffectivePolicy getSourcePolicy() {
        return this;
    }

    public EffectivePolicy getTargetPolicy() {
        return this;
    }

    public Set<PolicySet> getInterceptedEndpointPolicySets() {
        return Collections.emptySet();
    }

    public Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets() {
        return intercepted;
    }

    public PolicyMetadata getMetadata(LogicalOperation operation) {
        return metadata.get(operation);
    }

    public Set<Intent> getProvidedEndpointIntents() {
        return Collections.emptySet();
    }

    public Set<Intent> getAggregatedEndpointIntents() {
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

    public void addInterceptedPolicySets(LogicalOperation operation, List<PolicySet> sets) {
        intercepted.put(operation, sets);
    }

    public void addMetadata(LogicalOperation operation, PolicyMetadata value) {
        metadata.put(operation, value);
    }


}
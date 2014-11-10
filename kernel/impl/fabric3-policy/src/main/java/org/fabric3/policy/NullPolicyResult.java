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
 */
package org.fabric3.policy;

import java.util.Collections;
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
 * Used as a policy result stub.
 */
public class NullPolicyResult implements PolicyResult, EffectivePolicy {
    private PolicyMetadata metadata = new PolicyMetadata();

    public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
        return Collections.emptyList();
    }

    public Map<LogicalOperation, PolicyMetadata> getMetadata() {
        return Collections.emptyMap();
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
        return Collections.emptyMap();
    }

    public PolicyMetadata getMetadata(LogicalOperation operation) {
        return metadata;
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

}
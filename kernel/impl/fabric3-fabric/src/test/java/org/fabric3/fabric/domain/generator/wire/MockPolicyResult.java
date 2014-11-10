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
package org.fabric3.fabric.domain.generator.wire;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.domain.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 *
 */
public class MockPolicyResult implements PolicyResult {
    private PolicyMetadata metadata = new PolicyMetadata();
    private Map<LogicalOperation, List<PolicySet>> policies = new HashMap<>();

    public void addPolicy(LogicalOperation operation, List<PolicySet> list) {
        policies.put(operation, list);
    }

    public EffectivePolicy getSourcePolicy() {
        return null;
    }

    public EffectivePolicy getTargetPolicy() {
        return null;
    }

    public Set<PolicySet> getInterceptedEndpointPolicySets() {
        return Collections.emptySet();
    }

    public Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets() {
        return policies;
    }

    public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
        return policies.get(operation);
    }

    public Map<LogicalOperation, PolicyMetadata> getMetadata() {
        throw new UnsupportedOperationException();
    }

    public PolicyMetadata getMetadata(LogicalOperation operation) {
        return metadata;
    }

}
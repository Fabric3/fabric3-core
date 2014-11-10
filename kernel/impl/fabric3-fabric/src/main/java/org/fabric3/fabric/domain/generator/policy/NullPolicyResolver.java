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
package org.fabric3.fabric.domain.generator.policy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.policy.PolicyMetadata;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.domain.generator.policy.PolicyResolver;
import org.fabric3.spi.domain.generator.policy.PolicyResult;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * No-op resolver used during bootstrap.
 */
public class NullPolicyResolver implements PolicyResolver {
    private static final PolicyResult EMPTY_RESULT = new NullPolicyResult();

    public PolicyResult resolvePolicies(LogicalBinding<?> binding) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveCallbackPolicies(LogicalBinding<?> binding) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolvePolicies(LogicalConsumer consumer) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveLocalPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveLocalCallbackPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveRemotePolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    public PolicyResult resolveRemoteCallbackPolicies(LogicalWire wire) throws PolicyResolutionException {
        return EMPTY_RESULT;
    }

    private static class NullPolicyResult implements PolicyResult {
        private PolicyMetadata metadata = new PolicyMetadata();

        public Set<PolicySet> getInterceptedEndpointPolicySets() {
            return Collections.emptySet();
        }

        public Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets() {
            return Collections.emptyMap();
        }

        public List<PolicySet> getInterceptedPolicySets(LogicalOperation operation) {
            return Collections.emptyList();
        }

        public Map<LogicalOperation, PolicyMetadata> getMetadata() {
            return Collections.emptyMap();
        }

        public PolicyMetadata getMetadata(LogicalOperation operation) {
            return metadata;
        }

        public EffectivePolicy getSourcePolicy() {
            return new NullEffectivePolicy();
        }

        public EffectivePolicy getTargetPolicy() {
            return new NullEffectivePolicy();
        }

    }

    private static class NullEffectivePolicy implements EffectivePolicy {
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


}



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

import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.PolicyAttacher;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.model.instance.LogicalComponent;


/**
 * No-op attacher used during bootstrap.
 */
public class NullPolicyAttacher implements PolicyAttacher {

    public void attachPolicies(LogicalComponent<?> component, boolean incremental) {
        // no-op
    }

    public void attachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component, boolean incremental) throws PolicyResolutionException {
        // no-op
    }

    public void detachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component) throws PolicyResolutionException {
        // no-op
    }


}
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

import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Resolves implementation intents and policy sets. Resolution is performed by normalizing intents and policy sets for each operation associated with a wire.
 * <p/>
 * Note this is different than interaction resolution as endpoint- and operation-level policy is not distinguished. Since implementation policies are not
 * visible to clients, they are not handled at the transport level. Hence, the requirement by some transport protocols to distinguish policy assertions is not
 * applicable. Normalizing policy at the operation-level simplifies the resolver service contract and allows one-step resolution.
 */
public interface ImplementationPolicyResolver {

    /**
     * Returns the set of configured intents. The returned intent pair contains all intents configured for the implementation and provided by the implementation
     * according to the <code>mayProvide</code> attribute of the implementation type policy definition.
     *
     * @param component the logical component for which intents are to be resolved.
     * @param operation the operation for which the provided intents are to be computed.
     * @return Set of intents that need to be explicitly provided by the implementation.
     * @throws PolicyResolutionException If there are any unidentified intents.
     */
    IntentPair resolveIntents(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException;

    /**
     * Returns the set of policies explicitly declared for the operation and those that satisfy the intents not provided by the implementation type.
     *
     * @param component the logical component for which policies are to be resolved.
     * @param operation the operation for which the provided intents are to be computed.
     * @return Set of resolved policies.
     * @throws PolicyResolutionException If all intents cannot be resolved.
     */
    Set<PolicySet> resolvePolicySets(LogicalComponent<?> component, LogicalOperation operation) throws PolicyResolutionException;

}

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

import javax.xml.namespace.QName;
import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.policy.PolicyResolutionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalScaArtifact;

/**
 * Resolves interaction intents and policy sets. Resolution is performed for bindings, which aggregates intents and policy sets configured on a binding element
 * and its ancestors (e.g. services, references, and components). Resolution is also performed for operations, which evaluates intents and policies sets
 * explicitly configured at the operation-level.
 * <p/>
 * Resolution is performed in two steps to distinguish the effective policy that applies to an entire endpoint and that which applies to an operation. This is
 * necessary to account for protocols that require policy statements to be attached at specific points. For example, web services security, which mandates some
 * policy assertions only be placed on a WSDL binding (endpoint).
 */
public interface InteractionPolicyResolver {

    /**
     * Returns the set of intents configured for the binding and its ancestors. The returned intent pair contains all intents configured for the binding and
     * those that are explicitly provided by the binding extension through the <code>mayProvide</code> attribute.
     *
     * @param binding the binding configuration
     * @return the provided intents
     * @throws PolicyResolutionException if there are any unidentified intents
     */
    IntentPair resolveIntents(LogicalBinding binding) throws PolicyResolutionException;

    /**
     * Returns the set of intents configured for an operation. The returned intent pair contains all intents configured for the binding and that are explicitly
     * provided by the binding extension through the <code>mayProvide</code> attribute.
     *
     * @param operation   the operation
     * @param bindingType the binding type
     * @return the provided intents
     * @throws PolicyResolutionException if there are any unidentified intents
     */
    IntentPair resolveIntents(LogicalOperation operation, QName bindingType) throws PolicyResolutionException;

    /**
     * Returns the set of policies explicitly declared for the binding and its ancestors, including those that satisfy the intents not provided by the binding
     * type.
     *
     * @param binding the binding for which policies are to be resolved
     * @return the resolved policies
     * @throws PolicyResolutionException if all intents cannot be resolved
     */
    Set<PolicySet> resolvePolicySets(LogicalBinding binding) throws PolicyResolutionException;

    /**
     * Returns the set of policies explicitly declared for the operation and those that satisfy the intents not provided by the binding type.
     *
     * @param operation the operation for which the intents are to be resolved
     * @param artifact  the logical artifact where policy is applied
     * @param type      the binding type
     * @return the resolved policies
     * @throws PolicyResolutionException if all intents cannot be resolved
     */
    Set<PolicySet> resolvePolicySets(LogicalOperation operation, LogicalScaArtifact<?> artifact, QName type) throws PolicyResolutionException;

}

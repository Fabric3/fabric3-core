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
package org.fabric3.spi.domain.generator.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Represents the policy sets and intents that are to be applied to an endpoint and its operations.
 */
public interface EffectivePolicy {

    /**
     * Returns intents configured for an endpoint that are handled natively (provided) by the binding or implementation extension.
     *
     * @return the endpoint intents
     */
    Set<Intent> getProvidedEndpointIntents();

    /**
     * Returns all intents configured for an endpoint by the binding or implementation extension.
     *
     * @return the endpoint intents
     */
    Set<Intent> getAggregatedEndpointIntents();

    /**
     * Returns policy sets configured for an endpoint that are handled natively (provided) by the binding or implementation extension.
     *
     * @return the endpoint policy sets
     */
    Set<PolicySet> getEndpointPolicySets();

    /**
     * Returns intents configured for an operation that are handled natively (provided) by the binding or implementation extension.
     *
     * @param operation the operation
     * @return the provided intents
     */
    List<Intent> getIntents(LogicalOperation operation);

    /**
     * Returns intents configured for all endpoint operations that are handled natively (provided) by the binding extension.
     *
     * @return the provided intents
     */
    List<Intent> getOperationIntents();

    /**
     * Returns the policy sets for the the requested operation.
     *
     * @param operation the operation
     * @return the resolved policy sets
     */
    List<PolicySet> getPolicySets(LogicalOperation operation);

    /**
     * Returns the policy sets for all operations.
     *
     * @return Resolved policy sets that are provided mapped to their operation.
     */
    Map<LogicalOperation, List<PolicySet>> getOperationPolicySets();

}

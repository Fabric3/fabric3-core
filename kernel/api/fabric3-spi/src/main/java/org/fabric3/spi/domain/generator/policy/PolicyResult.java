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

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Result of resolving intents and policy sets for a wire or event stream.
 * <p/>
 * <p/>
 * The policies are resolved for the source and target bindings as well as the source and target component implementation types. A wire can be between
 * two components or between a component and a binding.
 * <p/>
 * For a wire between two components, the result will include: <ol>
 * <p/>
 * <li>Implementation intents that are requested for each operation on the source side and may be provided by the source component implementation
 * type.
 * <p/>
 * <li>Implementation intents that are requested for each operation on the target side and may be provided by the target component implementation
 * type.
 * <p/>
 * <li>Policy sets that map to implementation intents on each operation on the source side and understood by the source component
 * <p/>
 * implementation type. <li>Policy sets that map to implementation intents on each operation on the target side and understood by the target component
 * implementation type. <li>Policy sets that map to implementation intents on each operation on the source and target side that are implemented using
 * interceptors. </ol> For a wire between a binding and a component (service binding), the result will include: <ol> <li>Interaction intents that are
 * requested for each operation and may be provided by the service binding type. <li>Implementation intents that are requested for each operation and
 * may be  provided by the target component implementation type. <li>Policy sets that map to implementation intents on each operation and understood
 * by the component implementation type. <li>Policy sets that map to interaction intents on each operation on the source side and understood by the
 * service binding type. <li>Policy sets that map to implementation and interaction intents on each operation that are implemented using interceptors.
 * </ol> For a wire between a component and a binding (reference binding), the result will include: <ol> <li>Interaction intents that are requested
 * for each operation and may be provided by the reference binding type. <li>Implementation intents that are requested for each operation and may be
 * provided by the component implementation type. <li>Policy sets that map to implementation intents on each operation and understood by the component
 * implementation type. <li>Policy sets that map to interaction intents on each operation and understood by the service binding type. <li>Policy sets
 * that map to implementation and interaction intents on each operation that are implemented using interceptors. </ol>
 */
public interface PolicyResult {

    /**
     * Returns policies and intents provided at the source end of a wire.
     *
     * @return policies and intents provided at the source end of a wire
     */
    EffectivePolicy getSourcePolicy();

    /**
     * Returns policies and intents provided at the target end of a wire.
     *
     * @return policies and intents provided at the target end of a wire
     */
    EffectivePolicy getTargetPolicy();

    /**
     * Returns policies that are enforced at the endpoint level, i.e. for all operations.
     *
     * @return the policy sets
     */
    Set<PolicySet> getInterceptedEndpointPolicySets();

    /**
     * Returns policy sets that are enforced by an interceptor.
     *
     * @return the policy sets
     */
    Map<LogicalOperation, List<PolicySet>> getInterceptedPolicySets();

    /**
     * Returns policy sets that are enforced by an interceptor for an operation.
     *
     * @param operation operation against which interceptors are defined.
     * @return the policy sets
     */
    List<PolicySet> getInterceptedPolicySets(LogicalOperation operation);

    /**
     * Returns metadata for the intents and policy sets for a given operation.
     *
     * @return metadata for the intents and policy sets
     */
    Map<LogicalOperation, PolicyMetadata> getMetadata();

    /**
     * Returns metadata for the intents and policy sets for a given operation.
     *
     * @param operation the operation to return metadata for
     * @return metadata for the intents and policy sets
     */
    PolicyMetadata getMetadata(LogicalOperation operation);

}

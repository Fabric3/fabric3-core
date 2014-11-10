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

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalWire;


/**
 * Resolves applicable policy sets by expanding interaction and implementation intents and combining them with explicitly configured policy sets.
 */
public interface PolicyResolver {

    /**
     * Resolves policies for a bound service or reference.
     *
     * @param binding the service or reference binding
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolvePolicies(LogicalBinding<?> binding) throws PolicyResolutionException;

    /**
     * Resolves callback policies for a bound service or reference.
     *
     * @param binding the service or reference binding
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolveCallbackPolicies(LogicalBinding<?> binding) throws PolicyResolutionException;

    /**
     * Resolves policies for a local consumer.
     *
     * @param consumer the consumer
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolvePolicies(LogicalConsumer consumer) throws PolicyResolutionException;

    /**
     * Resolves policies for a local wire.
     *
     * @param wire the wire
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolveLocalPolicies(LogicalWire wire) throws PolicyResolutionException;

    /**
     * Resolves callback policies for a local wire.
     *
     * @param wire the wire
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolveLocalCallbackPolicies(LogicalWire wire) throws PolicyResolutionException;

    /**
     * Resolves policies for a remote wire.
     *
     * @param wire the wire
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolveRemotePolicies(LogicalWire wire) throws PolicyResolutionException;

    /**
     * Resolves callback policies for a remote wire.
     *
     * @param wire the wire
     * @return the resolved policy
     * @throws PolicyResolutionException if an error resolving policy occurs
     */
    PolicyResult resolveRemoteCallbackPolicies(LogicalWire wire) throws PolicyResolutionException;


}

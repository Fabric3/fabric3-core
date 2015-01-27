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

import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Applies policy using external policy attachment.
 */
public interface PolicyAttacher {

    /**
     * Attaches all active PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param component   the top-most component to evaluate external attachments against
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void attachPolicies(LogicalComponent<?> component) throws PolicyResolutionException;

    /**
     * Attaches PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param policySets  the policy sets to attach
     * @param component   the top-most component to evaluate external attachments against
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void attachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component) throws PolicyResolutionException;

    /**
     * Detaches PolicySets (i.e. those that use external attachment) to the component hierarchy.
     *
     * @param policySets the policy sets to detach
     * @param component  the top-most component to evaluate external attachments against
     * @throws PolicyResolutionException if an error occurs evaluating the policies
     */
    void detachPolicies(Set<PolicySet> policySets, LogicalComponent<?> component) throws PolicyResolutionException;

}

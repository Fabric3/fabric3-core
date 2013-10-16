/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.policy.resolver;

import java.util.Set;

import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.deployment.generator.policy.PolicyResolutionException;
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

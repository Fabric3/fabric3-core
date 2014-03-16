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

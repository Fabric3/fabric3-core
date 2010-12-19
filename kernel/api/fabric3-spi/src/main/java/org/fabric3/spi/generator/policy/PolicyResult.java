/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.generator.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.PolicyMetadata;
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
 *
 * @version $Rev$ $Date$
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

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
package org.fabric3.spi.policy;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Represents the policy sets and intents that are to be applied to an endpoint and its operations.
 *
 * @version $Rev$ $Date$
 */
public interface EffectivePolicy {

    /**
     * Returns effective intents configured for an endpoint that are handled natively (provided) by the binding or implementation extension.
     *
     * @return the endpoint intents
     */
    Set<Intent> getEndpointIntents();

    /**
     * Returns effective policy sets configured for an endpoint that are handled natively (provided) by the binding or implementation extension.
     *
     * @return the endpoint policy sets
     */
    Set<PolicySet> getEndpointPolicySets();

    /**
     * Returns effective intents configured for an operation that are handled natively (provided) by the binding or implementation extension.
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
     * Returns the effective policy sets for the the requested operation.
     *
     * @param operation the operation
     * @return the resolved policy sets
     */
    List<PolicySet> getPolicySets(LogicalOperation operation);

    /**
     * Returns the effective policy sets for all operations.
     *
     * @return Resolved policy sets that are provided mapped to their operation.
     */
    Map<LogicalOperation, List<PolicySet>> getOperationPolicySets();

}

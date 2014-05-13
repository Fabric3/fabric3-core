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
package org.fabric3.fabric.domain.instantiator.wire;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.domain.instantiator.AutowireResolver;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.oasisopen.sca.annotation.Reference;

/**
 * Autowires services using contract type matching.
 */
public class TypeAutowireResolver implements AutowireResolver {
    private ContractMatcher matcher;

    public TypeAutowireResolver(@Reference ContractMatcher matcher) {
        this.matcher = matcher;
    }

    public List<LogicalService> resolve(LogicalReference logicalReference, ServiceContract contract, LogicalCompositeComponent composite) {
        List<LogicalService> candidates = new ArrayList<>();
        Multiplicity refMultiplicity = logicalReference.getDefinition().getMultiplicity();
        boolean multiplicity = Multiplicity.ZERO_N.equals(refMultiplicity) || Multiplicity.ONE_N.equals(refMultiplicity);
        for (LogicalComponent<?> child : composite.getComponents()) {
            if (logicalReference.getParent() == child) {
                // don't wire to self
                continue;
            }
            if (validKey(logicalReference, child)) {  // if the reference is keyed and the target does not have a key, skip
                for (LogicalService service : child.getServices()) {
                    ServiceContract targetContract = service.getServiceContract();
                    if (targetContract == null) {
                        // This is a programming error since a non-composite service must have a service contract
                        throw new AssertionError("No service contract specified on service: " + service.getUri());
                    }
                    MatchResult result = matcher.isAssignableFrom(contract, targetContract, false);
                    if (result.isAssignable()) {
                        boolean intentsMatch = true;
                        for (QName intent : logicalReference.getIntents()) {
                            if (!service.getIntents().contains(intent)) {
                                intentsMatch = false;
                                break;
                            }
                        }
                        if (intentsMatch) {
                            candidates.add(service);
                            break;
                        }
                    }
                }
            }
            if (!candidates.isEmpty() && !multiplicity) {
                // since the reference is to a single target and a candidate has been found, avoid iterating the remaining components
                break;
            }
        }
        return candidates;
    }

    /**
     * Returns true if the reference is not keyed, true if the reference is keyed and the target specifies a key, false if the reference is keyed and the target
     * does not specify a key.
     *
     * @param logicalReference the logical reference
     * @param target           the target
     * @return true if the reference is not keyed, true if the reference is keyed and the target specifies a key, false if the reference is keyed and the target
     * does not specify a key
     */
    private boolean validKey(LogicalReference logicalReference, LogicalComponent<?> target) {
        return !logicalReference.getDefinition().isKeyed() || target.getDefinition().getKey() != null
               || target.getDefinition().getComponentType().getKey() != null;
    }
}

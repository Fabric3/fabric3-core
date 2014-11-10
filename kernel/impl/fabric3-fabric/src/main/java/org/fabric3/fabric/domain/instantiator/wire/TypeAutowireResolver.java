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

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

package org.fabric3.fabric.contract;

import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.api.model.type.java.Signature;
import static org.fabric3.spi.contract.MatchResult.MATCH;
import static org.fabric3.spi.contract.MatchResult.NO_MATCH;

/**
 * Compares JavaServiceContracts for compatibility.
 */
public class JavaContractMatcherExtension implements ContractMatcherExtension<JavaServiceContract, JavaServiceContract> {

    public Class<JavaServiceContract> getSource() {
        return JavaServiceContract.class;
    }

    public Class<JavaServiceContract> getTarget() {
        return JavaServiceContract.class;
    }

    public MatchResult isAssignableFrom(JavaServiceContract source, JavaServiceContract target, boolean reportErrors) {
        if (source == target) {
            return MATCH;
        }
        if ((source.getSuperType() == null && target.getSuperType() != null)
                || (source.getSuperType() != null && !source.getSuperType().equals(target.getSuperType()))) {
            if (reportErrors) {
                return new MatchResult("Types are not in the same type hierarchy");
            } else {
                return NO_MATCH;
            }
        }
        if (source.getInterfaceClass().equals(target.getInterfaceClass())) {
            for (Signature signature : source.getMethodSignatures()) {
                if (!target.getMethodSignatures().contains(signature)) {
                    if (reportErrors) {
                        return new MatchResult("Method signature not found on target service contract: " + signature);
                    } else {
                        return NO_MATCH;
                    }
                }
            }
            return MATCH;
        } else {
            // check the interfaces
            for (String superType : target.getInterfaces()) {
                if (superType.equals(source.getInterfaceClass())) {
                    // need to match params as well
                    return MATCH;
                }
            }
            if (!source.isRemotable() || !target.isRemotable()) {
                // enforce stricter compatibility rules for local interfaces
                if (reportErrors) {
                    return new MatchResult("Source and target interfaces do not match");
                } else {
                    return NO_MATCH;
                }

            }
            for (Signature signature : source.getMethodSignatures()) {
                if (!target.getMethodSignatures().contains(signature)) {
                    if (reportErrors) {
                        return new MatchResult("Method signature not found on target service contract: " + signature);
                    } else {
                        return NO_MATCH;
                    }
                }
            }
            return MATCH;
        }

    }

}

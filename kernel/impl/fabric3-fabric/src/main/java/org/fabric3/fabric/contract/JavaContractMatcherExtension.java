/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

package org.fabric3.fabric.contract;

import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.Signature;

/**
 * Compares JavaServiceContracts for compatibility.
 *
 * @version $Rev$ $Date$
 */
public class JavaContractMatcherExtension implements ContractMatcherExtension<JavaServiceContract, JavaServiceContract> {
    private static final MatchResult MATCH = new MatchResult(true);
    private static final MatchResult NO_MATCH = new MatchResult(false);

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

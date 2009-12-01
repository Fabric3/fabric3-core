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

package org.fabric3.wsdl.contract;

import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.contract.MatchResult;

/**
 * An abstract ContractMatcher that uses XML Schema to match contracts specified with different type systems.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractXsdContractMatcherExtension<S extends ServiceContract, T extends ServiceContract>
        implements ContractMatcherExtension<S, T> {
    private static final MatchResult MATCH = new MatchResult(true);
    private static final MatchResult NO_MATCH = new MatchResult(false);

    protected MatchResult matchContract(ServiceContract source, ServiceContract target, boolean reportErrors) {
        if (source == target) {
            return MATCH;
        }
        for (Operation operation : source.getOperations()) {
            MatchResult match = matchOperation(operation, target.getOperations(), reportErrors);
            if (!match.isAssignable()) {
                // fail fast
                return match;
            }
        }
        return MATCH;
    }

    // TODO throw explicit error if DataType.getXsdType() == null saying XSD mapping extension is not installed
    protected MatchResult matchOperation(Operation operation, List<Operation> operations, boolean reportErrors) {
        for (Operation candidate : operations) {
            String name = operation.getName();
            if (!name.equalsIgnoreCase(candidate.getName())) {
                continue;
            }
            // check input types
            List<DataType<?>> inputTypes = operation.getInputTypes();
            List<DataType<?>> candidateInputTypes = candidate.getInputTypes();
            if (inputTypes.size() != candidateInputTypes.size()) {
                if (reportErrors) {
                    return new MatchResult("The number of parameters for the source and target operations are not equal for operation " + name);
                } else {
                    return NO_MATCH;
                }
            }
            for (int i = 0; i < inputTypes.size(); i++) {
                DataType<?> inputType = inputTypes.get(i);
                DataType<?> candidateInputType = candidateInputTypes.get(i);
                if (inputType.getXsdType() == null || !inputType.getXsdType().equals(candidateInputType.getXsdType())) {
                    if (reportErrors) {
                        return new MatchResult("Input types at position " + i + " do not match on operation " + name
                                + ". Types were " + inputType.getXsdType() + " and " + candidateInputType.getXsdType());
                    } else {
                        return NO_MATCH;
                    }
                }
            }
            // check output types
            QName outputXsdType = operation.getOutputType().getXsdType();
            QName candidateOutputXsdType = candidate.getOutputType().getXsdType();
            if (outputXsdType == null || !outputXsdType.equals(candidateOutputXsdType)) {
                continue;
            }
            // check fault types
            // FIXME handle web faults
//            List<DataType<?>> faultTypes = operation.getFaultTypes();
//            List<DataType<?>> candidateFaultTypes = candidate.getFaultTypes();
//            for (int i = 0; i < faultTypes.size(); i++) {
//                DataType<?> faultType = faultTypes.get(i);
//                DataType<?> candidateFaultType = candidateFaultTypes.get(i);
//                if (faultType.getXsdType() == null || !faultType.getXsdType().equals(candidateFaultType.getXsdType())) {
//                    return false;
//                }
//            }
        }
        return MATCH;
    }

}
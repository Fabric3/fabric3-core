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

package org.fabric3.wsdl.contract;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcherExtension;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.model.type.xsd.XSDComplexType;
import org.fabric3.spi.model.type.xsd.XSDType;
import static org.fabric3.spi.contract.MatchResult.MATCH;
import static org.fabric3.spi.contract.MatchResult.NO_MATCH;

/**
 * An abstract ContractMatcher that uses XML Schema to match contracts specified with different type systems.
 */
public abstract class AbstractXsdContractMatcherExtension<S extends ServiceContract, T extends ServiceContract> implements ContractMatcherExtension<S, T> {

    protected MatchResult matchContract(ServiceContract source, ServiceContract target, boolean reportErrors) {
        if (source == target) {
            return MATCH;
        }
        if ((source.getCallbackContract() == null && target.getCallbackContract() != null) || (source.getCallbackContract() != null
                                                                                               && target.getCallbackContract() == null)) {
            if (reportErrors) {
                return new MatchResult("Callback contracts do not match");
            } else {
                return NO_MATCH;
            }
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
        boolean matched = false;
        for (Operation candidate : operations) {
            String name = operation.getName();
            String wsdlName = operation.getWsdlName();
            if (!name.equalsIgnoreCase(candidate.getName()) && !(wsdlName != null && wsdlName.equals(candidate.getWsdlName()))) {
                continue;
            }
            // check input types
            List<DataType> inputTypes = operation.getInputTypes();
            List<DataType> candidateInputTypes = candidate.getInputTypes();
            if (inputTypes.size() != candidateInputTypes.size()) {
                if (reportErrors) {
                    return new MatchResult("The number of parameters for the source and target operations are not equal for operation " + name);
                } else {
                    return NO_MATCH;
                }
            }
            for (int i = 0; i < inputTypes.size(); i++) {
                DataType inputType = inputTypes.get(i);
                DataType candidateInputType = candidateInputTypes.get(i);
                if (inputType.getXsdType() == null || !inputType.getXsdType().equals(candidateInputType.getXsdType())) {
                    if (inputType instanceof XSDComplexType) {
                        if (checkSequence((XSDComplexType) inputType, candidateInputType)) {
                            continue;
                        }
                    }
                    if (candidateInputType instanceof XSDComplexType) {
                        if (checkSequence((XSDComplexType) candidateInputType, inputType)) {
                            continue;
                        }
                    }
                    if (reportErrors) {
                        return new MatchResult("Input types at position " + i + " do not match on operation " + name);
                    } else {
                        return NO_MATCH;
                    }
                }
            }
            // check output types
            DataType outputType = operation.getOutputType();
            QName outputXsdType = outputType.getXsdType();
            DataType candidateOutputType = candidate.getOutputType();
            QName candidateOutputXsdType = candidateOutputType.getXsdType();
            if (outputXsdType == null || !outputXsdType.equals(candidateOutputXsdType)) {
                if (outputType instanceof XSDComplexType) {
                    if (checkSequence((XSDComplexType) outputType, candidateOutputType)) {
                        matched = true;
                        // found operation
                        break;
                    }
                }
                if (candidateOutputType instanceof XSDComplexType) {
                    if (checkSequence((XSDComplexType) candidateOutputType, outputType)) {
                        matched = true;
                        // found operation
                        break;
                    }
                }
                if (reportErrors) {
                    return new MatchResult("Output types do not match on operation " + name + ". Types were " + outputType.getXsdType() + " and "
                                           + candidateOutputType.getXsdType());
                } else {
                    return NO_MATCH;
                }
            }
            matched = true;
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
        if (matched) {
            return MATCH;
        } else {
            if (reportErrors) {
                return new MatchResult("No matching operation for: " + operation.getName());
            } else {
                return NO_MATCH;
            }
        }
    }

    /**
     * Attempts to match a XSD sequence against another type. This is triggered by JAXB when a sequence containing a single simple type is mapped to a single
     * Java type as in:
     * <p/>
     * <pre>
     * &lt;xs:complexType name="chair_kind"&gt;
     *    &lt;xs:sequence&gt;
     *    &lt;xs:element type="xs:boolean"/&gt;
     *    &lt;/xs:sequence&gt;
     * &lt;/xs:complexType&gt;
     * </pre>
     * <p/>
     * which is mapped to <code>setHasArmRest(boolean value)</code>
     *
     * @param complexType the complex type
     * @param type        the other type to compare
     * @return true if the types match
     */
    private boolean checkSequence(XSDComplexType complexType, DataType type) {
        if (complexType.isSequence() && complexType.getSequenceTypes().size() == 1) {
            XSDType sequenceType = complexType.getSequenceTypes().get(0);
            if (sequenceType.getXsdType().equals(type.getXsdType())) {
                // sequence type matches
                return true;
            }
            if (type.getXsdType().getNamespaceURI().equals("http://util.java/") && type.getXsdType().getLocalPart().equals("list")) {
                return true;
            }
        }
        return false;
    }

}
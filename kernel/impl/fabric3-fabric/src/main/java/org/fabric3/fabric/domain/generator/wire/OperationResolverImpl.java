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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.wire;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.contract.OperationNotFoundException;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.model.instance.LogicalAttachPoint;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.type.xsd.XSDComplexType;
import org.fabric3.spi.model.type.xsd.XSDType;

/**
 * Default implementation of the OperationResolver that resolves using the XML Schema type of the input parameters. Output parameters and faults are not
 * matched.
 */
public class OperationResolverImpl implements OperationResolver {

    public LogicalOperation resolve(LogicalOperation source, List<LogicalOperation> targets) throws OperationNotFoundException {
        Operation sourceDefinition = source.getDefinition();
        for (LogicalOperation target : targets) {
            Operation targetDefinition = target.getDefinition();

            // match on actual or mapped WSDL name
            if (sourceDefinition.getName().equals(targetDefinition.getName()) || sourceDefinition.getWsdlName().equals(targetDefinition.getWsdlName())) {
                List<DataType> sourceInputTypes = sourceDefinition.getInputTypes();
                List<DataType> targetInputTypes = targetDefinition.getInputTypes();
                DataType sourceOutputType = sourceDefinition.getOutputType();
                DataType targetOutputType = targetDefinition.getOutputType();
                if (sourceOutputType.equals(targetOutputType) && sourceInputTypes.equals(targetDefinition.getInputTypes())) {
                    return target;
                }
                if (sourceInputTypes.size() == targetInputTypes.size()) {
                    boolean equals = true;
                    for (int i = 0; i < sourceInputTypes.size(); i++) {
                        DataType sourceType = sourceInputTypes.get(i);
                        QName sourceXsdType = sourceType.getXsdType();
                        DataType targetType = targetInputTypes.get(i);
                        QName targetXsdType = targetType.getXsdType();
                        // compare by XSD type
                        if (sourceXsdType == null || targetXsdType == null || !sourceXsdType.equals(targetXsdType)) {
                            if (sourceType instanceof XSDComplexType && checkSequence((XSDComplexType) sourceType, targetType)) {
                                continue;
                            }
                            if (targetType instanceof XSDComplexType && checkSequence((XSDComplexType) targetType, sourceType)) {
                                continue;
                            }
                            equals = false;
                            break;
                        }
                    }
                    if (equals) {
                        return target;
                    }
                }
            }
        }
        LogicalAttachPoint parent = source.getParent();
        if (parent != null) {
            String sourceComponent = parent.getParent().getUri().toString();
            throw new OperationNotFoundException("Target operation not found for " + sourceDefinition.getName() + " on source component " + sourceComponent);
        } else {
            throw new OperationNotFoundException("Target operation not found for " + sourceDefinition.getName());
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
        }
        return false;
    }

}
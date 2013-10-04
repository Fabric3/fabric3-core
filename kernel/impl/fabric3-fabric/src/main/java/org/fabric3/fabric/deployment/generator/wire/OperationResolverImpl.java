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
package org.fabric3.fabric.deployment.generator.wire;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
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
                List<DataType<?>> sourceInputTypes = sourceDefinition.getInputTypes();
                List<DataType<?>> targetInputTypes = targetDefinition.getInputTypes();
                DataType<?> sourceOutputType = sourceDefinition.getOutputType();
                DataType<?> targetOutputType = targetDefinition.getOutputType();
                if (sourceOutputType.equals(targetOutputType) && sourceInputTypes.equals(targetDefinition.getInputTypes())) {
                    return target;
                }
                if (sourceInputTypes.size() == targetInputTypes.size()) {
                    boolean equals = true;
                    for (int i = 0; i < sourceInputTypes.size(); i++) {
                        DataType<?> sourceType = sourceInputTypes.get(i);
                        QName sourceXsdType = sourceType.getXsdType();
                        DataType<?> targetType = targetInputTypes.get(i);
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
    private boolean checkSequence(XSDComplexType complexType, DataType<?> type) {
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
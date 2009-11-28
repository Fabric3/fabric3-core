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
package org.fabric3.fabric.generator.wire;

import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Default implementation of the OperationResolver that resolves using the XML Schema type of the input parameters. Output parameters and faults are
 * not matched.
 *
 * @version $Rev: 7721 $ $Date: 2009-09-30 18:03:01 +0200 (Wed, 30 Sep 2009) $
 */
public class OperationResolverImpl implements OperationResolver {

    public LogicalOperation resolve(LogicalOperation source, List<LogicalOperation> targets) throws OperationNotFoundException {
        Operation sourceDefinition = source.getDefinition();
        for (LogicalOperation target : targets) {
            Operation targetDefinition = target.getDefinition();
            // match on actual or mapped WSDL name
            if (sourceDefinition.getName().equals(targetDefinition.getName())
                    || sourceDefinition.getWsdlName().equals(targetDefinition.getWsdlName())) {
                if (sourceDefinition.getInputTypes().size() == targetDefinition.getInputTypes().size()) {
                    List<DataType<?>> sourceTypes = sourceDefinition.getInputTypes();
                    boolean equals = true;
                    for (int i = 0; i < sourceTypes.size(); i++) {
                        DataType<?> sourceType = sourceTypes.get(i);
                        QName sourceXsdType = sourceType.getXsdType();
                        DataType<?> targetType = targetDefinition.getInputTypes().get(i);
                        QName targetXsdType = targetType.getXsdType();
                        // compare by XSD type
                        if (sourceXsdType == null || targetXsdType == null || !sourceXsdType.equals(targetXsdType)) {
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
        throw new OperationNotFoundException("Target operation not found for: " + sourceDefinition.getName());
    }

}
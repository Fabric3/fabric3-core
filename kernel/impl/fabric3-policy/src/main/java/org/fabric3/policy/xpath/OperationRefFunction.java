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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.model.type.component.AbstractReference;
import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Implements the OperationRef function defined by the SCA Policy Specification.
 */
public class OperationRefFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("Invalid number of arguments for OperationRef(): " + args.size());
        }
        Object arg = args.get(0);
        String[] tokens = arg.toString().split("/");
        if (tokens.length != 2) {
            throw new FunctionCallException("Invalid Service/Operation name: " + arg);
        }
        String interfaceName = tokens[0];
        String operationName = tokens[1];
        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        List<LogicalOperation> operations = new ArrayList<LogicalOperation>();
        for (LogicalComponent<?> component : nodeSet) {
            find(interfaceName, operationName, component, operations);
        }
        return operations;
    }

    private void find(String interfaceName, String operationName, LogicalComponent<?> component, List<LogicalOperation> operations) {
        for (LogicalService service : component.getServices()) {
            AbstractService definition = service.getDefinition();
            ServiceContract contract = definition.getServiceContract();
            // match on the name of the service contract but return the logical operation
            if (contract.getInterfaceName().equals(interfaceName)) {
                for (LogicalOperation operation : service.getOperations()) {
                    if (operation.getDefinition().getName().equals(operationName)) {
                        operations.add(operation);
                    }
                }
            }
        }
        for (LogicalReference reference : component.getReferences()) {
            AbstractReference definition = reference.getDefinition();
            // match on the name of the service contract but return the logical operation
            ServiceContract contract = definition.getServiceContract();
            if (contract.getInterfaceName().equals(interfaceName)) {
                for (LogicalOperation operation : reference.getOperations()) {
                    if (operation.getDefinition().getName().equals(operationName)) {
                        operations.add(operation);
                    }
                }
            }
        }
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                find(interfaceName, operationName, child, operations);
            }
        }

    }
}
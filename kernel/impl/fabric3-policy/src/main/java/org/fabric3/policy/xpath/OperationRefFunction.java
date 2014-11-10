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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.api.model.type.component.AbstractReference;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.contract.ServiceContract;
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
        List<LogicalOperation> operations = new ArrayList<>();
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
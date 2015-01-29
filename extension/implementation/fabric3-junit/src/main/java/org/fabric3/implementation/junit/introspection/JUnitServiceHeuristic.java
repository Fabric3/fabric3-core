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
package org.fabric3.implementation.junit.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.JavaType;
import org.junit.Test;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JUnitServiceHeuristic implements HeuristicProcessor {
    private static final String TEST_SERVICE_NAME = "testService";
    private static final List<DataType> INPUT_TYPE = Collections.emptyList();
    private static final JavaType OUTPUT_TYPE = new JavaType(void.class);
    private static final List<DataType> FAULT_TYPE = Collections.emptyList();

    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;

    public JUnitServiceHeuristic(@Reference IntrospectionHelper helper, @Reference JavaContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {

        JavaServiceContract testContract = generateTestContract(implClass);
        ServiceDefinition testService = new ServiceDefinition(TEST_SERVICE_NAME, testContract);
        componentType.add(testService);
        // if the class implements a single interface, use it, otherwise the contract is the class itself
        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);
        if (interfaces.size() > 1) {
            for (Class interfaze : interfaces) {
                if (interfaze.getCanonicalName().endsWith("Test")) {
                    continue;
                }
                ServiceDefinition serviceDefinition = createServiceDefinition(interfaze, componentType, context);
                componentType.add(serviceDefinition);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createServiceDefinition(Class<?> serviceInterface, InjectingComponentType componentType, IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context, componentType);
        return new ServiceDefinition(contract.getInterfaceName(), contract);
    }

    private JavaServiceContract generateTestContract(Class<?> implClass) {
        List<Operation> operations = new ArrayList<>();
        for (Method method : implClass.getMethods()) {
            // see if this is a test method
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.getReturnType() != void.class) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            String name = method.getName();
            if (!method.isAnnotationPresent(Test.class) && (name.length() < 5 || !name.startsWith("test"))) {
                continue;
            }
            Operation operation = new Operation(name, INPUT_TYPE, OUTPUT_TYPE, FAULT_TYPE);
            operations.add(operation);
        }
        JavaServiceContract contract = new JavaServiceContract(implClass);
        contract.setOperations(operations);
        return contract;
    }
}

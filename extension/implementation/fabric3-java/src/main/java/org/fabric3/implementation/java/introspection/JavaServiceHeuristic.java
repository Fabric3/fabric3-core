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
package org.fabric3.implementation.java.introspection;

import java.util.Set;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JavaServiceHeuristic implements HeuristicProcessor {
    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;
    private OperationPolicyIntrospector policyIntrospector;

    public JavaServiceHeuristic(@Reference IntrospectionHelper helper,
                                @Reference JavaContractProcessor contractProcessor,
                                @Reference OperationPolicyIntrospector policyIntrospector) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.policyIntrospector = policyIntrospector;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        // if any services have been defined, just introspect policy
        if (!componentType.getServices().isEmpty()) {
            for (AbstractService definition : componentType.getServices().values()) {
                JavaServiceContract contract = (JavaServiceContract) definition.getServiceContract();
                Class<?> serviceInterface;
                try {
                    serviceInterface = implClass.getClassLoader().loadClass(contract.getInterfaceClass());
                } catch (ClassNotFoundException e) {
                    // should not happen
                    throw new AssertionError(e);
                }
                policyIntrospector.introspectPolicyOnOperations(contract, implClass, context);
            }
            return;
        }

        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);
        if (interfaces.isEmpty()) {
            // no interfaces, use implementation
            ServiceDefinition serviceDefinition = createServiceDefinition(implClass, implClass, componentType, context);
            componentType.add(serviceDefinition);
        } else {
            // class implements all interfaces that are not management interfaces or in the Java package
            for (Class<?> interfaze : interfaces) {
                Package pkg = interfaze.getPackage();
                if (interfaze.isAnnotationPresent(Management.class) || pkg == null || pkg.getName().startsWith("java")) {
                    continue;
                }
                ServiceDefinition serviceDefinition = createServiceDefinition(interfaze, implClass, componentType, context);
                componentType.add(serviceDefinition);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createServiceDefinition(Class<?> serviceInterface,
                                                      Class<?> implClass,
                                                      ComponentType componentType,
                                                      IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context, componentType);
        ServiceDefinition definition = new ServiceDefinition(contract.getInterfaceName(), contract);
        policyIntrospector.introspectPolicyOnOperations(contract, implClass, context);
        return definition;
    }

}

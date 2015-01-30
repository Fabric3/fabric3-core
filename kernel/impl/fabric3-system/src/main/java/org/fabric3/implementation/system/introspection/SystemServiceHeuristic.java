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
package org.fabric3.implementation.system.introspection;

import java.util.Set;

import org.fabric3.api.model.type.component.ComponentType;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Heuristic that identifies the services provided by an implementation class.
 */
public class SystemServiceHeuristic implements HeuristicProcessor {
    private final JavaContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public SystemServiceHeuristic(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        // if the service contracts have not already been defined then introspect them
        if (componentType.getServices().isEmpty()) {
            // get the most specific interfaces implemented by the class
            Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);

            // if the class does not implement any interfaces, then the class itself is the service contract
            // we don't have to worry about proxies because all wires to system components are optimized
            if (interfaces.isEmpty()) {
                ServiceDefinition<ComponentType> serviceDefinition = createServiceDefinition(implClass, componentType, context);
                componentType.add(serviceDefinition);
            } else {
                // otherwise, expose all of the implemented interfaces
                for (Class<?> serviceInterface : interfaces) {
                    ServiceDefinition<ComponentType> serviceDefinition = createServiceDefinition(serviceInterface, componentType, context);
                    componentType.add(serviceDefinition);
                }
            }
        }

    }

    private ServiceDefinition<ComponentType> createServiceDefinition(Class<?> serviceInterface, InjectingComponentType componentType, IntrospectionContext
            context) {
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context, componentType);
        return new ServiceDefinition<>(contract.getInterfaceName(), contract);
    }
}

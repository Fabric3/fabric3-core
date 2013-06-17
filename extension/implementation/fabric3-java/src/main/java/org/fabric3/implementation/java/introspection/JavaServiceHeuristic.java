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
package org.fabric3.implementation.java.introspection;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class JavaServiceHeuristic implements HeuristicProcessor {
    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;
    private PolicyAnnotationProcessor policyProcessor;
    private OperationPolicyIntrospector policyIntrospector;

    public JavaServiceHeuristic(@Reference IntrospectionHelper helper,
                                @Reference JavaContractProcessor contractProcessor,
                                @Reference OperationPolicyIntrospector policyIntrospector) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.policyIntrospector = policyIntrospector;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
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
                introspectPolicy(serviceInterface, implClass, contract, definition, context);
            }
            return;
        }

        Set<Class<?>> interfaces = helper.getImplementedInterfaces(implClass);
        if (interfaces.size() == 1) {
            // The class implements a single interface, use it
            Class<?> service = interfaces.iterator().next();
            ServiceDefinition serviceDefinition = createServiceDefinition(service, implClass, componentType, context);
            componentType.add(serviceDefinition);
        } else if (interfaces.size() == 2) {
            // The class implements two interfaces. If one of them is a management interface, use the other
            // No management interfaces, use the impl class per SCA rules
            ServiceDefinition serviceDefinition = createServiceDefinition(implClass, implClass, componentType, context);
            componentType.add(serviceDefinition);
        } else {
            // multiple interfaces, use the impl class per SCA rules
            ServiceDefinition serviceDefinition = createServiceDefinition(implClass, implClass, componentType, context);
            componentType.add(serviceDefinition);
        }
    }

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createServiceDefinition(Class<?> serviceInterface,
                                                      Class<?> implClass,
                                                      ComponentType componentType,
                                                      IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context, componentType);
        ServiceDefinition definition = new ServiceDefinition(contract.getInterfaceName(), contract);
        introspectPolicy(serviceInterface, implClass, contract, definition, context);
        return definition;
    }

    private void introspectPolicy(Class<?> serviceInterface,
                                  Class<?> implClass,
                                  ServiceContract contract,
                                  AbstractService definition,
                                  IntrospectionContext context) {
        Annotation[] annotations = serviceInterface.getAnnotations();
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }

            policyIntrospector.introspectPolicyOnOperations(contract, implClass, context);

        }
    }

}

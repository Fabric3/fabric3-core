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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.java.annotation;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Processes the @Service annotation on a component implementation class.
 */
public class OASISServiceProcessor extends AbstractAnnotationProcessor<Service> {
    private final JavaContractProcessor contractProcessor;
    private OperationPolicyIntrospector policyIntrospector;

    public OASISServiceProcessor(JavaContractProcessor contractProcessor) {
        super(Service.class);
        this.contractProcessor = contractProcessor;
    }

    @Constructor
    public OASISServiceProcessor(@Reference JavaContractProcessor contractProcessor, @Reference OperationPolicyIntrospector policyIntrospector) {
        super(Service.class);
        this.contractProcessor = contractProcessor;
        this.policyIntrospector = policyIntrospector;
    }

    public void visitType(Service annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
        Class<?>[] services = annotation.value();
        String[] names = annotation.names();
        for (int i = 0; i < services.length; i++) {
            Class<?> service = services[i];
            componentType.add(createDefinition(service, names.length == 0 ? service.getSimpleName() : names[i], type, componentType, context));
        }
    }

    @SuppressWarnings({"unchecked"})
    private ServiceDefinition createDefinition(Class<?> service,
                                               String name,
                                               Class<?> implClass,
                                               InjectingComponentType componentType,
                                               IntrospectionContext context) {
        ServiceContract serviceContract = contractProcessor.introspect(service, implClass, context, componentType);
        ServiceDefinition definition = new ServiceDefinition(name, serviceContract);
        if (policyIntrospector != null) {
            policyIntrospector.introspectPolicyOnOperations(serviceContract, implClass, context);
        }
        return definition;
    }
}
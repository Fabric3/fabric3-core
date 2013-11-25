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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.java.annotation;

import java.lang.annotation.Annotation;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.policy.OperationPolicyIntrospector;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Processes the @Service annotation on a component implementation class.
 */
public class OASISServiceProcessor extends AbstractAnnotationProcessor<Service> {
    private final JavaContractProcessor contractProcessor;
    private PolicyAnnotationProcessor policyProcessor;
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

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void visitType(Service annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
        Class<?>[] services = annotation.value();
        String[] names = annotation.names();
        for (int i=0; i<services.length; i++) {
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
        Annotation[] annotations = service.getAnnotations();
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }
            policyIntrospector.introspectPolicyOnOperations(serviceContract, implClass, context);
        }
        return definition;
    }
}
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
package org.fabric3.jpa.introspection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.jpa.model.HibernateSessionResourceReference;
import org.fabric3.jpa.model.PersistenceContextResourceReference;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes @PersistenceContext annotations.
 */
@EagerInit
public class PersistenceContextProcessor extends AbstractAnnotationProcessor<PersistenceContext> {
    private ServiceContract factoryServiceContract;
    private IntrospectionHelper helper;

    public PersistenceContextProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(PersistenceContext.class);
        this.helper = helper;
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(EntityManager.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitField(PersistenceContext annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        String name = helper.getSiteName(field, null);
        if (EntityManager.class.equals(field.getType())) {
            PersistenceContextResourceReference definition = createDefinition(name, field, annotation, componentType, context);
            componentType.add(definition, site);
        } else {
            HibernateSessionResourceReference definition = createSessionDefinition(name, annotation, componentType);
            componentType.add(definition, site);
        }
    }

    public void visitMethod(PersistenceContext annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        String name = helper.getSiteName(method, null);
        if (EntityManager.class.equals(method.getParameterTypes()[0])) {
            PersistenceContextResourceReference definition = createDefinition(name, method, annotation, componentType, context);
            componentType.add(definition, site);
        } else {
            HibernateSessionResourceReference definition = createSessionDefinition(name, annotation, componentType);
            componentType.add(definition, site);
        }
    }

    private PersistenceContextResourceReference createDefinition(String name,
                                                                 Member member,
                                                                 PersistenceContext annotation,
                                                                 InjectingComponentType componentType,
                                                                 IntrospectionContext context) {
        String unitName = annotation.unitName();
        PersistenceContextType type = annotation.type();
        if (PersistenceContextType.EXTENDED == type) {
            InvalidPersistenceContextType error = new InvalidPersistenceContextType("Extended persistence contexts not supported: " + unitName,
                                                                                    member,
                                                                                    componentType);
            context.addError(error);
        }
        boolean multiThreaded = Scope.COMPOSITE == componentType.getScope();
        return new PersistenceContextResourceReference(name, unitName, factoryServiceContract, multiThreaded);
    }

    private HibernateSessionResourceReference createSessionDefinition(String name, PersistenceContext annotation, InjectingComponentType componentType) {
        String unitName = annotation.unitName();
        boolean multiThreaded = Scope.COMPOSITE == componentType.getScope();
        return new HibernateSessionResourceReference(name, unitName, factoryServiceContract, multiThreaded);
    }

}
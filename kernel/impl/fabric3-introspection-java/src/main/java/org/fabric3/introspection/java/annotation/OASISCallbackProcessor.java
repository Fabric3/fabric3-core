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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.component.Callback;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class OASISCallbackProcessor extends AbstractAnnotationProcessor<org.oasisopen.sca.annotation.Callback> {
    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    public OASISCallbackProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(org.oasisopen.sca.annotation.Callback.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }


    public void visitField(org.oasisopen.sca.annotation.Callback annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        validate(field, componentType, context);

        String name = helper.getSiteName(field, null);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Callback definition = createDefinition(name, type, implClass, componentType, context);
        componentType.add(definition, site);
    }

    public void visitMethod(org.oasisopen.sca.annotation.Callback annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        validate(method, componentType, context);

        String name = helper.getSiteName(method, null);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Callback definition = createDefinition(name, type, implClass, componentType, context);
        componentType.add(definition, site);
    }

    private void validate(Field field, InjectingComponentType componentType, IntrospectionContext context) {
        if (!Modifier.isProtected(field.getModifiers()) && !Modifier.isPublic(field.getModifiers())) {
            Class<?> clazz = field.getDeclaringClass();
            InvalidAccessor warning =
                    new InvalidAccessor("Illegal callback. The field " + field.getName() + " on " + clazz.getName()
                                                + " is annotated with @Callback and must be public or protected.", field, componentType);
            context.addError(warning);
        }
    }

    private void validate(Method method, InjectingComponentType componentType, IntrospectionContext context) {
        if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
            InvalidAccessor warning =
                    new InvalidAccessor("Illegal callback. The method " + method + " is annotated with @Callback and must be public or protected.",
                                        method,
                                        componentType);
            context.addError(warning);
        }
    }

    private Callback createDefinition(String name,
                                                Type type,
                                                Class<?> implClass,
                                                InjectingComponentType componentType,
                                                IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, typeMapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context, componentType);
        return new Callback(name, contract);
    }


}

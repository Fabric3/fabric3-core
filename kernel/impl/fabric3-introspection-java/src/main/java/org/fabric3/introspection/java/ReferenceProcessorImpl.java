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
 */
package org.fabric3.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.fabric3.api.annotation.Target;
import org.fabric3.api.annotation.Targets;
import org.fabric3.api.model.type.component.Multiplicity;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.ReferenceProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */

public class ReferenceProcessorImpl implements ReferenceProcessor {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    public ReferenceProcessorImpl(@org.oasisopen.sca.annotation.Reference JavaContractProcessor contractProcessor,
                                  @org.oasisopen.sca.annotation.Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void addDefinition(Field field, String name, boolean required, Class<?> clazz, InjectingComponentType componentType, IntrospectionContext context) {
        name = helper.getSiteName(field, name);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Annotation[] annotations = field.getAnnotations();
        Reference definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
        componentType.add(definition, site);
        addTargets(field, field, context, definition);
    }

    public void addDefinition(Method method,
                              String name,
                              boolean required,
                              Class<?> clazz,
                              InjectingComponentType componentType,
                              IntrospectionContext context) {
        name = helper.getSiteName(method, name);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Annotation[] annotations = method.getAnnotations();
        Reference definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
        addTargets(method, method, context, definition);
        componentType.add(definition, site);
    }

    public void addDefinition(Constructor constructor,
                              String name,
                              int index,
                              boolean required,
                              Class<?> clazz,
                              InjectingComponentType componentType,
                              IntrospectionContext context) {
        name = helper.getSiteName(constructor, index, name);
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Annotation[] annotations = constructor.getParameterAnnotations()[index];
        Reference definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
        componentType.add(definition, site);
        addTargets(constructor, constructor, context, definition);

    }

    private Reference createDefinition(String name,
                                       boolean required,
                                       Type type,
                                       Class<?> implClass,
                                       Annotation[] annotations,
                                       InjectingComponentType componentType,
                                       IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, typeMapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context, componentType);
        Reference definition = new Reference(name, contract);
        helper.processMultiplicity(definition, required, type, typeMapping);
        return definition;
    }

    private void addTargets(AccessibleObject accessibleObject, Member member, IntrospectionContext context, Reference definition) {
        Targets targetsAnnotation = accessibleObject.getAnnotation(Targets.class);
        if (targetsAnnotation != null) {
            Multiplicity multiplicity = definition.getMultiplicity();
            if (multiplicity != Multiplicity.ONE_N && multiplicity != Multiplicity.ZERO_N) {
                Class<?> clazz = member.getDeclaringClass();
                String name = member.getName();
                InvalidAnnotation error = new InvalidAnnotation("Reference is not a multiplicity: " + name, accessibleObject, targetsAnnotation, clazz);
                context.addError(error);
                return;
            }
            for (String value : targetsAnnotation.value()) {
                org.fabric3.api.model.type.component.Target target = parseTarget(value, targetsAnnotation, accessibleObject, member, context);
                if (target != null) {
                    definition.addTarget(target);
                }
            }
        } else {
            Target targetAnnotation = accessibleObject.getAnnotation(Target.class);
            if (targetAnnotation != null) {
                org.fabric3.api.model.type.component.Target target = parseTarget(targetAnnotation.value(), targetAnnotation, accessibleObject, member, context);
                if (target != null) {
                    definition.addTarget(target);
                }
            }
        }
    }

    private org.fabric3.api.model.type.component.Target parseTarget(String target,
                                                                    Annotation annotation,
                                                                    AccessibleObject accessibleObject,
                                                                    Member member,
                                                                    IntrospectionContext context) {
        String[] tokens = target.split("/");
        if (tokens.length == 1) {
            return new org.fabric3.api.model.type.component.Target(tokens[0]);
        } else if (tokens.length == 2) {
            return new org.fabric3.api.model.type.component.Target(tokens[0], tokens[1]);
        } else if (tokens.length == 3) {
            return new org.fabric3.api.model.type.component.Target(tokens[0], tokens[1], tokens[2]);
        } else {
            Class<?> clazz = member.getDeclaringClass();
            String name = member.getName();
            InvalidAnnotation error = new InvalidAnnotation("Invalid target format: " + target + " on " + name, accessibleObject, annotation, clazz);
            context.addError(error);
            return null;
        }
    }
}

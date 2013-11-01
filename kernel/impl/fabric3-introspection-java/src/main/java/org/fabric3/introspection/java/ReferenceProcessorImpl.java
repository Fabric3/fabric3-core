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
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.ReferenceProcessor;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */

public class ReferenceProcessorImpl implements ReferenceProcessor {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;
    private PolicyAnnotationProcessor policyProcessor;

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public ReferenceProcessorImpl(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void addDefinition(Field field, String name, boolean required, Class<?> clazz, InjectingComponentType componentType, IntrospectionContext context) {
        name = helper.getSiteName(field, name);
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Annotation[] annotations = field.getAnnotations();
        ReferenceDefinition definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
        componentType.add(definition, site);
        addTargets(field, field, context, definition);
    }

    public void addDefinition(Method method, String name, boolean required, Class<?> clazz, InjectingComponentType componentType,
                              IntrospectionContext context) {
        name = helper.getSiteName(method, name);
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Annotation[] annotations = method.getAnnotations();
        ReferenceDefinition definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
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
        ReferenceDefinition definition = createDefinition(name, required, type, clazz, annotations, componentType, context);
        componentType.add(definition, site);
        addTargets(constructor, constructor, context, definition);

    }

    private ReferenceDefinition createDefinition(String name,
                                                 boolean required,
                                                 Type type,
                                                 Class<?> implClass,
                                                 Annotation[] annotations,
                                                 InjectingComponentType componentType,
                                                 IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, typeMapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context, componentType);
        ReferenceDefinition definition = new ReferenceDefinition(name, contract);
        helper.processMultiplicity(definition, required, type, typeMapping);
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, definition, context);
            }
        }
        return definition;
    }

    private void addTargets(AccessibleObject accessibleObject, Member member, IntrospectionContext context, ReferenceDefinition definition) {
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

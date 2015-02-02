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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.annotation.Producer;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.MultiplicityType;
import org.fabric3.spi.introspection.java.NoConstructorFound;
import org.fabric3.spi.introspection.java.UnknownInjectionType;
import org.fabric3.spi.introspection.java.annotation.AmbiguousConstructor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 *
 */
public class JavaHeuristic implements HeuristicProcessor {
    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;

    private final HeuristicProcessor serviceHeuristic;

    public JavaHeuristic(@org.oasisopen.sca.annotation.Reference IntrospectionHelper helper,
                         @org.oasisopen.sca.annotation.Reference JavaContractProcessor contractProcessor,
                         @org.oasisopen.sca.annotation.Reference(name = "service") HeuristicProcessor serviceHeuristic) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.serviceHeuristic = serviceHeuristic;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {

        // apply service heuristic
        serviceHeuristic.applyHeuristics(componentType, implClass, context);

        if (componentType.getConstructor() == null) {
            Signature ctor = findConstructor(implClass, componentType, context);
            componentType.setConstructor(ctor);
        }

        if (componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResourceReferences().isEmpty()) {
            evaluateConstructor(componentType, implClass, context);
            evaluateSetters(componentType, implClass, context);
            evaluateFields(componentType, implClass, context);
        }

        String scope = componentType.getScope();
        if (componentType.isManaged() && !Scope.getScope(scope).isSingleton()) {
            IllegalManagementAttribute warning = new IllegalManagementAttribute(implClass, componentType);
            context.addWarning(warning);
        }

    }

    private Signature findConstructor(Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.oasisopen.sca.annotation.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass, componentType));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                // no annotation present, look for a ctor with @Reference or @Producer or @Monitor
                for (Constructor<?> constructor : constructors) {
                    for (Annotation[] annotations : constructor.getParameterAnnotations()) {
                        for (Annotation annotation : annotations) {
                            if (annotation.annotationType().equals(org.oasisopen.sca.annotation.Reference.class)
                                || annotation.annotationType().equals(Producer.class) || annotation.annotationType().equals(Monitor.class)) {
                                if (selected != null) {
                                    context.addError(new AmbiguousConstructor(implClass, componentType));
                                    return null;
                                }
                                selected = constructor;
                            }
                        }
                    }
                }
                if (selected == null) {
                    context.addError(new NoConstructorFound(implClass, componentType));
                    return null;
                }
            }
        }
        return new Signature(selected);
    }

    private void evaluateConstructor(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            Signature ctor = componentType.getConstructor();
            if (ctor == null) {
                // there could have been an error evaluating the constructor, in which case no signature will be present
                return;
            }
            constructor = ctor.getConstructor(implClass);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new AssertionError(e);
        }

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Class<?> parameterType = helper.getBaseType(parameterTypes[i], typeMapping);

            String name = helper.getSiteName(constructor, i, null);
            processSite(componentType, name, constructor, parameterType, implClass, site, context);
        }
    }

    private void evaluateSetters(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Set<Method> setters = helper.getInjectionMethods(implClass, componentType.getServices().values());
        for (Method setter : setters) {
            InjectionSite site = new MethodInjectionSite(setter, 0);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(setter, null);
            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Type genericType = setter.getGenericParameterTypes()[0];
            Class<?> parameterType = helper.getBaseType(genericType, typeMapping);
            processSite(componentType, name, setter, parameterType, implClass, site, context);
        }
    }

    private void evaluateFields(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Set<Field> fields = helper.getInjectionFields(implClass);
        for (Field field : fields) {
            InjectionSite site = new FieldInjectionSite(field);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(field, null);
            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Class<?> parameterType = helper.getBaseType(field.getGenericType(), typeMapping);
            processSite(componentType, name, field, parameterType, implClass, site, context);
        }
    }

    private void processSite(InjectingComponentType componentType,
                             String name,
                             Member member,
                             Class<?> parameterType,
                             Class<?> declaringClass,
                             InjectionSite site, IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        InjectableType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
            case PROPERTY:
                addProperty(componentType, name, parameterType, declaringClass, site, context);
                break;
            case REFERENCE:
                addReference(componentType, name, parameterType, declaringClass, site, context);
                break;
            case CALLBACK:
                context.addError(new UnknownInjectionType(site, type, componentType.getImplClass(), member, componentType));
                break;
            default:
                context.addError(new UnknownInjectionType(site, type, componentType.getImplClass(), member, componentType));
        }
    }

    private void addProperty(InjectingComponentType componentType,
                             String name,
                             Type type,
                             Class<?> declaringClass,
                             InjectionSite site,
                             IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        Property property = new Property(name);
        MultiplicityType multiplicityType = helper.introspectMultiplicity(type, typeMapping);
        property.setMany(MultiplicityType.COLLECTION == multiplicityType || MultiplicityType.DICTIONARY == multiplicityType);
        componentType.add(property, site);
    }

    @SuppressWarnings({"unchecked"})
    private void addReference(InjectingComponentType componentType,
                              String name,
                              Class<?> parameterType,
                              Class<?> declaringClass,
                              InjectionSite site,
                              IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        ServiceContract contract = contractProcessor.introspect(parameterType, context, componentType);
        Reference reference = new Reference(name, contract);
        helper.processMultiplicity(reference, false, parameterType, typeMapping);
        componentType.add(reference, site);
    }
}

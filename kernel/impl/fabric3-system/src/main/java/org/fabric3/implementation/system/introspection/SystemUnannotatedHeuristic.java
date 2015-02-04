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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Property;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.MultiplicityType;
import org.fabric3.spi.introspection.java.UnknownInjectionType;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Heuristic processor that locates unannotated Property and Reference dependencies.
 */
public class SystemUnannotatedHeuristic implements HeuristicProcessor {

    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    public SystemUnannotatedHeuristic(@org.oasisopen.sca.annotation.Reference IntrospectionHelper helper,
                                      @org.oasisopen.sca.annotation.Reference JavaContractProcessor contractProcessor) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {

        // if any properties, references or resources have been defined already assume that was what the user intended and return
        if (!(componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResourceReferences().isEmpty())) {
            return;
        }

        evaluateConstructor(componentType, implClass, context);
        evaluateSetters(componentType, implClass, context);
        evaluateFields(componentType, implClass, context);
    }

    void evaluateConstructor(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Constructor<?> constructor = componentType.getConstructor();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            Type parameterType = parameterTypes[i];
            String name = helper.getSiteName(constructor, i, null);
            processSite(componentType, typeMapping, name, constructor, parameterType, site, context);
        }
    }

    void evaluateSetters(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Set<Method> setters = helper.getInjectionMethods(implClass, componentType.getServices().values());
        for (Method setter : setters) {
            InjectionSite site = new MethodInjectionSite(setter, 0);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(setter, null);
            Type parameterType = setter.getGenericParameterTypes()[0];
            processSite(componentType, typeMapping, name, setter, parameterType, site, context);
        }
    }

    void evaluateFields(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Set<Field> fields = helper.getInjectionFields(implClass);
        for (Field field : fields) {
            InjectionSite site = new FieldInjectionSite(field);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(field, null);
            Type parameterType = field.getGenericType();
            processSite(componentType, typeMapping, name, field, parameterType, site, context);
        }
    }

    private void processSite(InjectingComponentType componentType,
                             TypeMapping typeMapping,
                             String name,
                             Member member,
                             Type parameterType,
                             InjectionSite site,
                             IntrospectionContext context) {
        InjectableType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
            case PROPERTY:
                addProperty(componentType, typeMapping, name, parameterType, site);
                break;
            case REFERENCE:
                addReference(componentType, typeMapping, name, parameterType, site, context);
                break;
            default:
                String clazz = componentType.getImplClass().getName();
                UnknownInjectionType error = new UnknownInjectionType(site, type, clazz, member, componentType);
                context.addError(error);
        }
    }

    private void addProperty(InjectingComponentType componentType, TypeMapping typeMapping, String name, Type type, InjectionSite site) {
        Property property = new Property(name);
        MultiplicityType multiplicityType = helper.introspectMultiplicity(type, typeMapping);
        property.setMany(MultiplicityType.COLLECTION == multiplicityType || MultiplicityType.DICTIONARY == multiplicityType);
        componentType.add(property, site);
    }

    private void addReference(InjectingComponentType componentType,
                              TypeMapping typeMapping,
                              String name,
                              Type parameterType,
                              InjectionSite site,
                              IntrospectionContext context) {
        Class<?> type = helper.getBaseType(parameterType, typeMapping);
        ServiceContract contract = contractProcessor.introspect(type, context, componentType);
        Reference<ComponentType> reference = new Reference<>(name, contract);
        helper.processMultiplicity(reference, false, parameterType, typeMapping);
        componentType.add(reference, site);
    }
}

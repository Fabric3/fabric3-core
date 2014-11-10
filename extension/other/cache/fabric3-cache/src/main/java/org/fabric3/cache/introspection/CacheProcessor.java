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
package org.fabric3.cache.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.Cache;
import org.fabric3.cache.model.CacheReferenceDefinition;
import org.fabric3.cache.spi.MissingCacheName;
import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;

/**
 * Introspects fields, methods and constructor parameters annotated with {@link Cache}.
 */
@EagerInit
public class CacheProcessor extends AbstractAnnotationProcessor<Cache> {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    public CacheProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(Cache.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(Cache annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(field, null);
        FieldInjectionSite site = new FieldInjectionSite(field);
        Class<?> type = field.getType();
        ResourceReferenceDefinition definition = create(name, annotation, type, field, componentType, context);
        componentType.add(definition, site);
    }

    public void visitMethod(Cache annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        if (method.getParameterTypes().length != 1) {
            InvalidCacheSetter error = new InvalidCacheSetter("Setter must contain one parameter: " + method, method, componentType);
            context.addError(error);
            return;
        }
        String name = helper.getSiteName(method, null);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Class<?> type = method.getParameterTypes()[0];
        ResourceReferenceDefinition definition = create(name, annotation, type, method, componentType, context);
        componentType.add(definition, site);
    }

    public void visitConstructorParameter(Cache annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
        String name = annotation.name();
        Class<?> type = constructor.getParameterTypes()[index];
        ResourceReferenceDefinition definition = create(name, annotation, type, constructor, componentType, context);
        componentType.add(definition);
    }

    private ResourceReferenceDefinition create(String name,
                                               Cache annotation,
                                               Class<?> type,
                                               Member member,
                                               InjectingComponentType componentType,
                                               IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(type, context, componentType);
        String cacheName = annotation.name();
        if (cacheName.length() == 0) {
            MissingCacheName error = new MissingCacheName(member, componentType);
            context.addError(error);
            return new CacheReferenceDefinition(name, contract, false, "error");
        }
        return new CacheReferenceDefinition(name, contract, false, cacheName);
    }


}

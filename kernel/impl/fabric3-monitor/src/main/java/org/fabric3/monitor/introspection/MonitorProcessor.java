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
package org.fabric3.monitor.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.monitor.model.MonitorResourceReference;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class MonitorProcessor extends AbstractAnnotationProcessor<Monitor> {
    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;

    public MonitorProcessor(@Reference IntrospectionHelper helper, @Reference JavaContractProcessor contractProcessor) {
        super(Monitor.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Monitor annotation, Field field, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(field, null);
        Type genericType = field.getGenericType();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        FieldInjectionSite site = new FieldInjectionSite(field);
        MonitorResourceReference resource = createDefinition(name, annotation, type, componentType, context);
        componentType.add(resource, site);
    }

    public void visitMethod(Monitor annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        String name = helper.getSiteName(method, null);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Type genericType = helper.getGenericType(method);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        MonitorResourceReference resource = createDefinition(name, annotation, type, componentType, context);
        componentType.add(resource, site);
    }

    public void visitConstructorParameter(Monitor annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {
        String name = helper.getSiteName(constructor, index, null);
        Type genericType = helper.getGenericType(constructor, index);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        MonitorResourceReference resource = createDefinition(name, annotation, type, componentType, context);
        componentType.add(resource, site);
    }

    private MonitorResourceReference createDefinition(String name,
                                                      Monitor annotation,
                                                      Class<?> type,
                                                      ComponentType componentType,
                                                      IntrospectionContext context) {
        ServiceContract contract = contractProcessor.introspect(type, context, componentType);
        String destinationName = annotation.value();
        if (destinationName.length() == 0) {
            return new MonitorResourceReference(name, contract);
        } else {
            return new MonitorResourceReference(name, contract, destinationName);
        }
    }
}

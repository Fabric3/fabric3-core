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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.contract.TypeIntrospector;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects {@link org.fabric3.api.annotation.Consumer} annotations.
 */
@EagerInit
public class ConsumerProcessor extends AbstractAnnotationProcessor<org.fabric3.api.annotation.Consumer> {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    private List<TypeIntrospector> typeIntrospectors = Collections.emptyList();

    @Reference(required = false)
    public void setTypeIntrospectors(List<TypeIntrospector> typeIntrospectors) {
        this.typeIntrospectors = typeIntrospectors;
    }

    public ConsumerProcessor(IntrospectionHelper helper) {
        super(org.fabric3.api.annotation.Consumer.class);
        this.helper = helper;
    }

    @Constructor
    public ConsumerProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(org.fabric3.api.annotation.Consumer.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitConstructorParameter(org.fabric3.api.annotation.Consumer annotation,
                                          java.lang.reflect.Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {

        String name = helper.getSiteName(constructor, index, annotation.value());

        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> genericType = constructor.getParameterTypes()[index];
        Type logicalParamType = typeMapping.getActualType(genericType);
        DataType dataType = createDataType(genericType, logicalParamType, typeMapping);

        ServiceContract contract = null;
        if (contractProcessor != null) {
            Class<?> baseType = helper.getBaseType(genericType, typeMapping);
            contract = contractProcessor.introspect(baseType, implClass, context, componentType);

        }

        ConstructorInjectionSite injectionSite = new ConstructorInjectionSite(constructor, index);
        Consumer<ComponentType> consumer = new Consumer<>(name, dataType, contract);

        if (annotation.group().length() > 0){
            consumer.setGroup(annotation.group());
        }

        processSources(annotation, consumer, constructor, constructor.getDeclaringClass(), context);
        componentType.add(consumer, injectionSite, constructor);

    }

    public void visitField(org.fabric3.api.annotation.Consumer annotation,
                           Field field,
                           Class<?> implClass,
                           InjectingComponentType componentType,
                           IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);

        Class<?> genericType = field.getType();
        Type logicalParamType = typeMapping.getActualType(genericType);
        DataType dataType = createDataType(genericType, logicalParamType, typeMapping);

        String name = helper.getSiteName(field, annotation.value());
        ServiceContract contract = null;
        if (contractProcessor != null) {
            Class<?> baseType = helper.getBaseType(genericType, typeMapping);
            contract = contractProcessor.introspect(baseType, implClass, context, componentType);

        }
        Consumer<ComponentType> consumer = new Consumer<>(name, dataType, contract);

        if (annotation.group().length() > 0){
            consumer.setGroup(annotation.group());
        }
        Class<?> clazz = field.getDeclaringClass();
        processSources(annotation, consumer, field, clazz, context);
        FieldInjectionSite injectionSite = new FieldInjectionSite(field);
        componentType.add(consumer, injectionSite, field);

    }

    public void visitMethod(org.fabric3.api.annotation.Consumer annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        if (method.getParameterTypes().length > 1) {
            InvalidConsumerMethod failure = new InvalidConsumerMethod("Consumer method " + method + " has more than one parameter", method, componentType);
            context.addError(failure);
            return;
        }
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        DataType type = introspectParameterType(method, typeMapping);

        String name = helper.getSiteName(method, annotation.value());

        ServiceContract contract = null;
        if (contractProcessor != null) {
            Class<?> baseType = helper.getBaseType(type.getType(), typeMapping);
            contract = contractProcessor.introspect(baseType, implClass, context, componentType);

        }
        Consumer<ComponentType> consumer = new Consumer<>(name, type, contract);

        if (annotation.group().length() > 0){
            consumer.setGroup(annotation.group());
        }
        int sequence = annotation.sequence();
        if (sequence < 0) {
            context.addError(new InvalidConsumerMethod("Sequence number cannot be negative: " + method, method, componentType));
        } else {
            consumer.setSequence(sequence);
        }
        Class<?> clazz = method.getDeclaringClass();
        processSources(annotation, consumer, method, clazz, context);
        MethodInjectionSite injectionSite = new MethodInjectionSite(method, 0);
        componentType.add(consumer, injectionSite, method);
    }

    private DataType introspectParameterType(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalParameterTypes = method.getParameterTypes();
        Type gParamType = method.getGenericParameterTypes()[0];
        Type logicalParamType = typeMapping.getActualType(gParamType);
        return createDataType(physicalParameterTypes[0], logicalParamType, typeMapping);
    }

    @SuppressWarnings({"unchecked"})
    private DataType createDataType(Class<?> physicalType, Type type, TypeMapping mapping) {
        JavaType dataType;
        if (type instanceof Class) {
            // not a generic
            dataType = new JavaType(physicalType);
        } else {
            JavaTypeInfo info = helper.createTypeInfo(type, mapping);
            dataType = new JavaGenericType(info);
        }
        for (TypeIntrospector introspector : typeIntrospectors) {
            introspector.introspect(dataType);
        }
        return dataType;
    }

    private void processSources(org.fabric3.api.annotation.Consumer annotation,
                                Consumer consumer,
                                AccessibleObject member,
                                Class<?> clazz,
                                IntrospectionContext context) {
        try {
            if (annotation.sources().length > 0) {
                for (String target : annotation.sources()) {
                    consumer.addSource(new URI(target));
                }
            } else if (annotation.source().length() > 0) {
                consumer.addSource(new URI(annotation.source()));
            }
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid consumer source on : " + clazz.getName(), member, annotation, clazz, e);
            context.addError(error);
        }
    }

}
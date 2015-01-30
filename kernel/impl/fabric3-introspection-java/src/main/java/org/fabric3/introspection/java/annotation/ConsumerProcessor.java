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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.component.Consumer;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.TypeIntrospector;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.java.JavaTypeInfo;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects {@link org.fabric3.api.annotation.Consumer} annotations.
 */
@EagerInit
public class ConsumerProcessor extends AbstractAnnotationProcessor<org.fabric3.api.annotation.Consumer> {
    private IntrospectionHelper helper;

    private List<TypeIntrospector> typeIntrospectors = Collections.emptyList();

    @Reference(required = false)
    public void setTypeIntrospectors(List<TypeIntrospector> typeIntrospectors) {
        this.typeIntrospectors = typeIntrospectors;
    }

    public ConsumerProcessor(@Reference IntrospectionHelper helper) {
        super(org.fabric3.api.annotation.Consumer.class);
        this.helper = helper;
    }

    public void visitMethod(org.fabric3.api.annotation.Consumer annotation, Method method, Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        if (method.getParameterTypes().length > 1) {
            InvalidConsumerMethod failure = new InvalidConsumerMethod("Consumer method " + method + " has more than one parameter", method, componentType);
            context.addError(failure);
            return;
        }
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        List<DataType> types = introspectParameterTypes(method, typeMapping);
        // TODO handle policies
        String name = helper.getSiteName(method, annotation.value());
        Signature signature = new Signature(method);
        Consumer consumer = new Consumer(name, types);

        int sequence = annotation.sequence();
        if (sequence < 0) {
            context.addError(new InvalidConsumerMethod("Sequence number cannot be negative: " + method, method, componentType));
        } else {
            consumer.setSequence(sequence);
        }
        Class<?> clazz = method.getDeclaringClass();
        processSources(annotation, consumer, method, clazz, context);
        componentType.add(consumer, signature);
    }

    private List<DataType> introspectParameterTypes(Method method, TypeMapping typeMapping) {
        Class<?>[] physicalParameterTypes = method.getParameterTypes();
        Type[] gParamTypes = method.getGenericParameterTypes();
        List<DataType> parameterDataTypes = new ArrayList<>(gParamTypes.length);
        for (int i = 0; i < gParamTypes.length; i++) {
            Type gParamType = gParamTypes[i];
            Type logicalParamType = typeMapping.getActualType(gParamType);
            DataType dataType = createDataType(physicalParameterTypes[i], logicalParamType, typeMapping);
            parameterDataTypes.add(dataType);
        }
        return parameterDataTypes;
    }

    @SuppressWarnings({"unchecked"})
    private DataType createDataType(Class<?> physicalType, Type type, TypeMapping mapping) {
        JavaType dataType;
        if (type instanceof Class) {
            // not a generic
            dataType = new JavaType(physicalType);
        } else {
            JavaTypeInfo info = helper.createTypeInfo(type, mapping);
            dataType= new JavaGenericType(info);
        }
        for (TypeIntrospector introspector : typeIntrospectors) {
            introspector.introspect(dataType);
        }
        return dataType;
    }

    private void processSources(org.fabric3.api.annotation.Consumer annotation, Consumer consumer, AccessibleObject member, Class<?> clazz, IntrospectionContext context) {
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
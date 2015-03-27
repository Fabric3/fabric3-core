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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Producer;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects {@link org.fabric3.api.annotation.Producer} annotations.
 */
@EagerInit
public class ProducerProcessor extends AbstractAnnotationProcessor<org.fabric3.api.annotation.Producer> {
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    public ProducerProcessor(@Reference JavaContractProcessor contractProcessor, @Reference IntrospectionHelper helper) {
        super(org.fabric3.api.annotation.Producer.class);
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public void visitField(org.fabric3.api.annotation.Producer annotation,
                           Field field,
                           Class<?> implClass,
                           InjectingComponentType componentType,
                           IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.value());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        Producer<ComponentType> producer = createProducer(name, type, implClass, componentType, field, context);

        Class<?> clazz = field.getDeclaringClass();
        processTargets(annotation, producer, field, clazz, context);

        componentType.add(producer, site);
    }

    public void visitMethod(org.fabric3.api.annotation.Producer annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {

        String name = helper.getSiteName(method, annotation.value());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        Producer<ComponentType> definition = createProducer(name, type, implClass, componentType, method, context);
        Class<?> clazz = method.getDeclaringClass();
        processTargets(annotation, definition, method, clazz, context);
        componentType.add(definition, site);
    }

    public void visitConstructorParameter(org.fabric3.api.annotation.Producer annotation,
                                          Constructor<?> constructor,
                                          int index,
                                          Class<?> implClass,
                                          InjectingComponentType componentType,
                                          IntrospectionContext context) {

        String name = helper.getSiteName(constructor, index, annotation.value());
        Type type = helper.getGenericType(constructor, index);
        ConstructorInjectionSite site = new ConstructorInjectionSite(constructor, index);
        Producer<ComponentType> producer = createProducer(name, type, implClass, componentType, constructor, context);
        Class<?> clazz = constructor.getDeclaringClass();
        processTargets(annotation, producer, constructor, clazz, context);
        componentType.add(producer, site);
    }

    private Producer<ComponentType> createProducer(String name,
                                                   Type type,
                                                   Class<?> implClass,
                                                   InjectingComponentType componentType,
                                                   Member member,
                                                   IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> baseType = helper.getBaseType(type, typeMapping);
        ServiceContract contract = contractProcessor.introspect(baseType, implClass, context, componentType);
        if (contract.getOperations().isEmpty()) {
            String interfaceName = contract.getInterfaceName();
            InvalidProducerInterface error = new InvalidProducerInterface("Producer interfaces must have at least one method: " + interfaceName,
                                                                          member,
                                                                          componentType);
            context.addError(error);
        }
        return new Producer<>(name, contract);
    }

    private void processTargets(org.fabric3.api.annotation.Producer annotation,
                                Producer producer,
                                AnnotatedElement element,
                                Class<?> clazz,
                                IntrospectionContext context) {
        try {
            if (annotation.targets().length > 0) {
                for (String target : annotation.targets()) {
                    producer.addTarget(new URI(target));
                }
            } else if (annotation.target().length() > 0) {
                producer.addTarget(new URI(annotation.target()));
            }
        } catch (URISyntaxException e) {

            InvalidAnnotation error = new InvalidAnnotation("Invalid producer target on : " + clazz.getName(), element, annotation, clazz, e);
            context.addError(error);
        }
    }

}
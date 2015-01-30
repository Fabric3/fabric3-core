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
package org.fabric3.spi.introspection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.fabric3.api.annotation.model.BindingHandler;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.BindingHandlerDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Base class for introspecting binding information in a component implementation.
 */
@EagerInit
public abstract class AbstractBindingPostProcessor<A extends Annotation> implements PostProcessor {
    private Class<A> annotationType;
    private Method serviceAttribute;

    protected AbstractBindingPostProcessor(Class<A> annotationType) {
        this.annotationType = annotationType;
        try {
            serviceAttribute = annotationType.getDeclaredMethod("service");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Binding annotation must have a service attribute");
        }
    }

    public void process(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        processService(componentType, implClass, context);
        processReferences(componentType, implClass, context);
    }

    protected String getNullibleValue(String value) {
        return value.isEmpty() ? null : value;
    }

    protected abstract BindingDefinition processService(A annotation,
                                                        ServiceDefinition<ComponentType> service,
                                                        InjectingComponentType componentType,
                                                        Class<?> implClass,
                                                        IntrospectionContext context);

    protected abstract BindingDefinition processServiceCallback(A annotation,
                                                                ServiceDefinition<ComponentType> service,
                                                                InjectingComponentType componentType,
                                                                Class<?> implClass,
                                                                IntrospectionContext context);

    protected abstract BindingDefinition processReference(A annotation,
                                                          ReferenceDefinition reference,
                                                          AccessibleObject object,
                                                          Class<?> implClass,
                                                          IntrospectionContext context);

    protected abstract BindingDefinition processReferenceCallback(A annotation,
                                                                  ReferenceDefinition reference,
                                                                  AccessibleObject object,
                                                                  Class<?> implClass,
                                                                  IntrospectionContext context);

    private void processService(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        A annotation = implClass.getAnnotation(annotationType);
        if (annotation == null) {
            return;
        }
        Class<?> serviceInterface = getService(annotation);
        if (serviceInterface.equals(Void.class)) {
            serviceInterface = null;
        }
        ServiceDefinition<ComponentType> boundService = null;
        if (serviceInterface == null) {
            if (componentType.getServices().size() != 1) {
                InvalidAnnotation error = new InvalidAnnotation("Binding annotation must specify a service interface", implClass, annotation, implClass);
                context.addError(error);
                return;
            }
            boundService = componentType.getServices().values().iterator().next();
        } else {
            String name = serviceInterface.getName();
            for (ServiceDefinition<ComponentType> service : componentType.getServices().values()) {
                String interfaceName = service.getServiceContract().getQualifiedInterfaceName();
                if (interfaceName.equals(name)) {
                    boundService = service;
                    break;
                }
            }
            if (boundService == null) {
                InvalidAnnotation error = new InvalidAnnotation("Service specified in binding annotation not found: " + name, implClass, annotation, implClass);
                context.addError(error);
                return;
            }
        }
        BindingDefinition binding = processService(annotation, boundService, componentType, implClass, context);
        if (binding == null) {
            return;
        }
        processHandlers(implClass, binding, implClass, context);
        boundService.addBinding(binding);

        ServiceContract contract = boundService.getServiceContract();
        if (contract.getCallbackContract() != null) {
            BindingDefinition callbackBinding = processServiceCallback(annotation, boundService, componentType, implClass, context);
            if (callbackBinding != null) {
                boundService.addCallbackBinding(callbackBinding);
            }
        }

    }

    private void processReferences(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        for (Map.Entry<ModelObject, InjectionSite> entry : componentType.getInjectionSiteMappings().entrySet()) {
            if (!(entry.getKey() instanceof ReferenceDefinition)) {
                continue;
            }
            ReferenceDefinition reference = (ReferenceDefinition) entry.getKey();
            InjectionSite site = entry.getValue();
            if (site instanceof FieldInjectionSite) {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = fieldSite.getField();
                processBindingAnnotation(field, reference, implClass, context);
            } else if (site instanceof MethodInjectionSite) {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getMethod();
                processBindingAnnotation(method, reference, implClass, context);
            } else if (site instanceof ConstructorInjectionSite) {
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                Constructor<?> constructor = constructorSite.getConstructor();
                Annotation[] annotations = constructor.getParameterAnnotations()[constructorSite.getParam()];
                for (Annotation annotation : annotations) {
                    if (annotationType.equals(annotation.annotationType())) {
                        A castAnnotation = annotationType.cast(annotation);
                        BindingDefinition binding = processReference(castAnnotation, reference, constructor, implClass, context);
                        if (binding == null) {
                            continue;
                        }
                        reference.addBinding(binding);
                        ServiceContract contract = reference.getServiceContract();
                        if (contract.getCallbackContract() != null) {
                            BindingDefinition callbackBinding = processReferenceCallback(castAnnotation, reference, constructor, implClass, context);
                            if (callbackBinding != null) {
                                reference.addCallbackBinding(callbackBinding);
                            }
                        }
                    }
                }
            }
        }

    }

    private void processBindingAnnotation(AccessibleObject object, ReferenceDefinition reference, Class<?> implClass, IntrospectionContext context) {
        A annotation = object.getAnnotation(annotationType);
        if (annotation == null) {
            return;
        }
        BindingDefinition binding = processReference(annotation, reference, object, implClass, context);
        if (binding == null) {
            return;
        }
        reference.addBinding(binding);
        ServiceContract contract = reference.getServiceContract();
        if (contract.getCallbackContract() != null) {
            BindingDefinition callbackBinding = processReferenceCallback(annotationType.cast(annotation), reference, object, implClass, context);
            if (callbackBinding != null) {
                reference.addCallbackBinding(callbackBinding);
            }
        }

    }

    private void processHandlers(AnnotatedElement element, BindingDefinition binding, Class<?> implClass, IntrospectionContext context) {
        BindingHandler annotation = element.getAnnotation(BindingHandler.class);
        if (annotation == null) {
            return;
        }
        if (annotation.value().isEmpty()) {
            String[] values = annotation.handlers();
            for (String value : values) {
                parseHandlerUri(value, element, binding, implClass, context, annotation);
            }
        } else {
            parseHandlerUri(annotation.value(), element, binding, implClass, context, annotation);
        }
    }

    private void parseHandlerUri(String value,
                                 AnnotatedElement element,
                                 BindingDefinition binding,
                                 Class<?> implClass,
                                 IntrospectionContext context,
                                 BindingHandler annotation) {
        try {
            BindingHandlerDefinition definition = new BindingHandlerDefinition(new URI(value));
            binding.addHandler(definition);
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid binding handler URI", element, annotation, implClass, e);
            context.addError(error);
        }
    }

    private Class<?> getService(A annotation) {
        try {
            return (Class<?>) serviceAttribute.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

}

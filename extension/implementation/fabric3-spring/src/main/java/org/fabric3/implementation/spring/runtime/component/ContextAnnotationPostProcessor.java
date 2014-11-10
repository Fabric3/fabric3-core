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
package org.fabric3.implementation.spring.runtime.component;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.fabric3.api.Fabric3RequestContext;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.annotation.Context;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Processes and handles injection for Spring bean properties annotated with {@link Context}.
 */
public class ContextAnnotationPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements MergedBeanDefinitionPostProcessor {
    private final Map<Class<?>, InjectionMetadata> cache = new HashMap<>();

    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = buildContextMetadata(beanType);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = getMetadata(bean.getClass());
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Context injection failed", ex);
        }
        return pvs;
    }

    private InjectionMetadata getMetadata(Class<?> clazz) {
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = cache.get(clazz);
        if (metadata == null) {
            metadata = buildContextMetadata(clazz);
            cache.put(clazz, metadata);
        }
        return metadata;
    }

    private InjectionMetadata buildContextMetadata(Class<?> clazz) {
        LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<>();
        Class<?> targetClass = clazz;

        do {
            LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList<>();
            for (Field field : targetClass.getDeclaredFields()) {
                Annotation annotation = field.getAnnotation(Context.class);
                if (annotation != null) {
                    if (!RequestContext.class.equals(field.getType()) && !Fabric3RequestContext.class.equals(field.getType())) {
                        throw new BeanCreationException("Invalid @Context type on field: " + field);
                    }
                    currElements.add(new ContextFieldElement(field));
                }
            }
            for (Method method : targetClass.getDeclaredMethods()) {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                boolean methodPair = BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod);
                Annotation annotation = methodPair ? bridgedMethod.getAnnotation(Context.class) : method.getAnnotation(Context.class);
                if (annotation != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
                    if (method.getParameterTypes().length != 1) {
                        throw new BeanCreationException("Method annotated with @Context must take one parameter: " + method);
                    }
                    Class<?> param = method.getParameterTypes()[0];
                    if (!RequestContext.class.equals(param) && !Fabric3RequestContext.class.equals(param)) {
                        throw new BeanCreationException("Invalid @Context type on field: " + method);
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
                    currElements.add(new ContextMethodElement(method, pd));
                }
            }
            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        } while (targetClass != null && targetClass != Object.class);

        return new InjectionMetadata(clazz, elements);
    }

    private class ContextFieldElement extends InjectionMetadata.InjectedElement {
        public ContextFieldElement(Field field) {
            super(field, null);
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            try {
                ReflectionUtils.makeAccessible(field);
                SpringRequestContext context = new SpringRequestContext(beanName);
                field.set(bean, context);
            } catch (Throwable ex) {
                throw new BeanCreationException("Could not inject context field: " + field, ex);
            }
        }
    }

    private class ContextMethodElement extends InjectionMetadata.InjectedElement {

        public ContextMethodElement(Method method, PropertyDescriptor pd) {
            super(method, pd);
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            if (checkPropertySkipping(pvs)) {
                return;
            }
            Method method = (Method) this.member;
            try {
                ReflectionUtils.makeAccessible(method);
                SpringRequestContext context = new SpringRequestContext(beanName);
                method.invoke(bean, context);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            } catch (Throwable ex) {
                throw new BeanCreationException("Could not inject context method: " + method, ex);
            }
        }

    }
}

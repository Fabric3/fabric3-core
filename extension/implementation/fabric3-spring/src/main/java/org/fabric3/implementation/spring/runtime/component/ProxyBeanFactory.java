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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * A BeanFactory implementation that tracks wire and event stream proxies configured for a Spring component. This factory is used by a parent of the
 * Spring application context for a given component and will resolve wire or channel proxies that are configured for contained Spring beans.
 */
public class ProxyBeanFactory extends DefaultListableBeanFactory {
    private static final long serialVersionUID = -7196391297579217924L;

    private transient Map<String, ObjectFactory> factories = new ConcurrentHashMap<>();
    private transient Map<String, BeanDefinition> definitions = new ConcurrentHashMap<>();

    public void add(String name, Class<?> type, ObjectFactory factory) {
        factories.put(name, factory);
        BeanDefinition definition = new GenericBeanDefinition();
        definition.setBeanClassName(type.getName());
        definition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        definitions.put(name, definition);
    }

    public ObjectFactory remove(String name) {
        definitions.remove(name);
        return factories.remove(name);
    }

    @Override
    public Object getBean(String name) throws BeansException {
        ObjectFactory factory = factories.get(name);
        if (factory == null) {
            return super.getBean(name);
        }
        try {
            return factory.getInstance();
        } catch (ObjectCreationException e) {
            throw new BeanCreationException("Error creating proxy: " + name, e);
        }
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        Object o = getBean(name);
        if (o == null || requiredType == null) {
            // requiredType may be null when passed from Spring
            return (T) o;
        }
        if (requiredType.isInstance(o)) {
            return requiredType.cast(o);
        } else {
            throw new BeanNotOfRequiredTypeException(name, requiredType, o.getClass());
        }
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        // proxies never take arguments
        return getBean(name);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
        return definitions.get(beanName);
    }

    @Override
    public boolean containsBean(String name) {
        return factories.containsKey(name);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return factories.containsKey(beanName);
    }

    @Override
    public int getBeanDefinitionCount() {
        return factories.size();
    }

    @Override
    public String[] getBeanDefinitionNames() {
        return definitions.keySet().toArray(new String[definitions.size()]);
    }

    @Override
    public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        // proxies are never singletons
        return false;
    }

    @Override
    public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
        // proxies are always stateless
        return true;
    }

}
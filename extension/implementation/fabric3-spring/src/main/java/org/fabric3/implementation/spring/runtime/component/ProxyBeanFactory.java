/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * A BeanFactory implementation that tracks wire and event stream proxies configured for a Spring component. This factory is used by a parent of the
 * Spring application context for a given component and will resolve wire or channel proxies that are configured for contained Spring beans.
 *
 * @version $Rev$ $Date$
 */
public class ProxyBeanFactory extends DefaultListableBeanFactory {
    private static final long serialVersionUID = -7196391297579217924L;

    private Map<String, ObjectFactory> factories = new ConcurrentHashMap<String, ObjectFactory>();
    private Map<String, BeanDefinition> definitions = new ConcurrentHashMap<String, BeanDefinition>();

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
            return null;
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
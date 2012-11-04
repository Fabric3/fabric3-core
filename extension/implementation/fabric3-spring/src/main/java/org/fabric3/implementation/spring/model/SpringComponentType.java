/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.spring.model;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * A component type for a Spring application context.
 */
public class SpringComponentType extends InjectingComponentType {
    private static final long serialVersionUID = -8894733979791676532L;

    // bean id to definition
    private Map<String, BeanDefinition> beansById = new HashMap<String, BeanDefinition>();
    // bean name to definition
    private Map<String, BeanDefinition> beansByName = new HashMap<String, BeanDefinition>();

    public void add(BeanDefinition definition) {
        String id = definition.getId();
        String name = definition.getName();
        if (name == null && id == null) {
            throw new IllegalArgumentException("Bean must have an id or name");
        }
        if (id != null) {
            beansById.put(id, definition);
        }
        if (name != null) {
            beansByName.put(name, definition);
        }
    }

    public Map<String, BeanDefinition> getBeansById() {
        return beansById;
    }

    public Map<String, BeanDefinition> getBeansByName() {
        return beansByName;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void add(ServiceDefinition service) {
        if (!(service instanceof SpringService)) {
            throw new IllegalArgumentException("Service type must be " + SpringService.class.getName());
        }
        super.add(service);
    }

    public Map<String, SpringService> getSpringServices() {
        return cast(getServices());
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }

}

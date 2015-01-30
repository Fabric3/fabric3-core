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
package org.fabric3.implementation.spring.model;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * A component type for a Spring application context.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class SpringComponentType extends InjectingComponentType {
    private static final long serialVersionUID = -8894733979791676532L;

    // bean id to definition
    private Map<String, BeanDefinition> beansById = new HashMap<>();
    // bean name to definition
    private Map<String, BeanDefinition> beansByName = new HashMap<>();

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

    public void add(Service<ComponentType> service) {
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

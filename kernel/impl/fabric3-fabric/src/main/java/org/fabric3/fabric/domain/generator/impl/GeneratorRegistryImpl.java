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
package org.fabric3.fabric.domain.generator.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.fabric.domain.generator.GeneratorNotFoundException;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {
    private Map<Class<?>, ComponentGenerator<?>> componentGenerators = new HashMap<>();
    private Map<Class<?>, WireBindingGenerator<?>> bindingGenerators = new HashMap<>();
    private Map<Class<?>, ConnectionBindingGenerator<?>> connectionBindingGenerators = new ConcurrentHashMap<>();
    private List<InterceptorGenerator> interceptorGenerators = Collections.emptyList();
    private Map<Class<?>, ResourceReferenceGenerator<?>> resourceReferenceGenerators = new HashMap<>();
    private Map<Class<?>, ResourceGenerator<?>> resourceGenerators = new HashMap<>();

    @Reference(required = false)
    public void setComponentGenerators(Map<Class<?>, ComponentGenerator<?>> componentGenerators) {
        this.componentGenerators = componentGenerators;
    }

    @Reference(required = false)
    public void setBindingGenerators(Map<Class<?>, WireBindingGenerator<?>> bindingGenerators) {
        this.bindingGenerators = bindingGenerators;
    }

    @Reference(required = false)
    public void setConnectionBindingGenerators(Map<Class<?>, ConnectionBindingGenerator<?>> bindingGenerators) {
        this.connectionBindingGenerators = bindingGenerators;
    }

    @Reference(required = false)
    public void setResourceReferenceGenerators(Map<Class<?>, ResourceReferenceGenerator<?>> resourceReferenceGenerators) {
        this.resourceReferenceGenerators = resourceReferenceGenerators;
    }

    @Reference(required = false)
    public void setInterceptorGenerators(List<InterceptorGenerator> interceptorGenerators) {
        this.interceptorGenerators = interceptorGenerators;
    }

    @Reference(required = false)
    public void setResourceGenerators(Map<Class<?>, ResourceGenerator<?>> resourceGenerators) {
        this.resourceGenerators = resourceGenerators;
    }

    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    public <T extends ResourceReferenceDefinition> void register(Class<T> clazz, ResourceReferenceGenerator<T> generator) {
        resourceReferenceGenerators.put(clazz, generator);
    }

    public <T extends BindingDefinition> void register(Class<T> clazz, WireBindingGenerator<T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    @SuppressWarnings("unchecked")
    public <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ComponentGenerator<LogicalComponent<T>> generator = (ComponentGenerator<LogicalComponent<T>>) componentGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends BindingDefinition> WireBindingGenerator<T> getBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        WireBindingGenerator<T> generator = (WireBindingGenerator<T>) bindingGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends BindingDefinition> ConnectionBindingGenerator<T> getConnectionBindingGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ConnectionBindingGenerator<T> generator = (ConnectionBindingGenerator<T>) connectionBindingGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceReferenceDefinition> ResourceReferenceGenerator<T> getResourceReferenceGenerator(Class<T> clazz)
            throws GeneratorNotFoundException {
        ResourceReferenceGenerator<T> generator = (ResourceReferenceGenerator<T>) resourceReferenceGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

    public List<InterceptorGenerator> getInterceptorGenerators() {
        return interceptorGenerators;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends ResourceDefinition> ResourceGenerator<T> getResourceGenerator(Class<T> clazz) throws GeneratorNotFoundException {
        ResourceGenerator<T> generator = (ResourceGenerator<T>) resourceGenerators.get(clazz);
        if (generator == null) {
            throw new GeneratorNotFoundException(clazz);
        }
        return generator;
    }

}

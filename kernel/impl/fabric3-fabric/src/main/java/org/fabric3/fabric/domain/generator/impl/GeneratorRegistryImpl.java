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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.ComponentGenerator;
import org.fabric3.spi.domain.generator.ResourceGenerator;
import org.fabric3.spi.domain.generator.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.InterceptorGenerator;
import org.fabric3.spi.domain.generator.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class GeneratorRegistryImpl implements GeneratorRegistry {
    @Reference(required = false)
    protected Map<Class<?>, ComponentGenerator<?>> componentGenerators = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, WireBindingGenerator<?>> bindingGenerators = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, ConnectionBindingGenerator<?>> connectionBindingGenerators = new HashMap<>();

    @Reference(required = false)
    protected List<InterceptorGenerator> interceptorGenerators = new ArrayList<>();

    @Reference(required = false)
    protected Map<Class<?>, ResourceReferenceGenerator<?>> resourceReferenceGenerators = new HashMap<>();

    @Reference(required = false)
    protected Map<Class<?>, ResourceGenerator<?>> resourceGenerators = new HashMap<>();

    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    public <T extends ResourceReference> void register(Class<T> clazz, ResourceReferenceGenerator<T> generator) {
        resourceReferenceGenerators.put(clazz, generator);
    }

    public <T extends Binding> void register(Class<T> clazz, WireBindingGenerator<T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    @SuppressWarnings("unchecked")
    public <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) {
        ComponentGenerator<LogicalComponent<T>> generator = (ComponentGenerator<LogicalComponent<T>>) componentGenerators.get(clazz);
        if (generator == null) {
            throw new Fabric3Exception("Component generator not found for " + clazz.getName());
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends Binding> WireBindingGenerator<T> getBindingGenerator(Class<T> clazz) {
        WireBindingGenerator<T> generator = (WireBindingGenerator<T>) bindingGenerators.get(clazz);
        if (generator == null) {
            throw new Fabric3Exception("Wire binding generator not found for " + clazz.getName());
        }
        return generator;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Binding> ConnectionBindingGenerator<T> getConnectionBindingGenerator(Class<T> clazz) {
        ConnectionBindingGenerator<T> generator = (ConnectionBindingGenerator<T>) connectionBindingGenerators.get(clazz);
        if (generator == null) {
            throw new Fabric3Exception("Connection binding generator not found for " + clazz.getName());
        }
        return generator;
    }

    @SuppressWarnings("unchecked")
    public <T extends ResourceReference> ResourceReferenceGenerator<T> getResourceReferenceGenerator(Class<T> clazz) {
        ResourceReferenceGenerator<T> generator = (ResourceReferenceGenerator<T>) resourceReferenceGenerators.get(clazz);
        if (generator == null) {
            throw new Fabric3Exception("Resource reference generator not found for " + clazz.getName());
        }
        return generator;
    }

    public List<InterceptorGenerator> getInterceptorGenerators() {
        return interceptorGenerators;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Resource> ResourceGenerator<T> getResourceGenerator(Class<T> clazz) {
        ResourceGenerator<T> generator = (ResourceGenerator<T>) resourceGenerators.get(clazz);
        if (generator == null) {
            throw new Fabric3Exception("Resource generator not found for " + clazz.getName());
        }
        return generator;
    }

}

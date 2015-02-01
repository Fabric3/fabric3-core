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
package org.fabric3.fabric.domain.generator;

import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.domain.generator.wire.InterceptorGenerator;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * A registry of generators. Generators are responsible for producing physical model objects that are provisioned to service nodes from their logical
 * counterparts.
 */
public interface GeneratorRegistry {

    /**
     * Returns a {@link CommandGenerator} for the specified implementation.
     *
     * @param clazz the implementation type the generator handles.
     * @return a the component generator for that implementation type
     * @throws Fabric3Exception if no generator is registered for the implementation type
     */
    <T extends Implementation<?>> ComponentGenerator<LogicalComponent<T>> getComponentGenerator(Class<T> clazz) throws Fabric3Exception;

    /**
     * Returns a {@link WireBindingGenerator} for the specified binding class.
     *
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws Fabric3Exception if no generator is registered for the binding type
     */
    <T extends Binding> WireBindingGenerator<T> getBindingGenerator(Class<T> clazz) throws Fabric3Exception;

    /**
     * Returns a {@link ConnectionBindingGenerator} for the specified binding class.
     *
     * @param clazz The binding type type the generator handles.
     * @return The registered binding generator.
     * @throws Fabric3Exception if no generator is registered for the binding type
     */
    <T extends Binding> ConnectionBindingGenerator<?> getConnectionBindingGenerator(Class<T> clazz) throws Fabric3Exception;

    /**
     * Returns the {@link ResourceReferenceGenerator} for the resource type.
     *
     * @param clazz the resource type the generator handles
     * @return the resource reference generator
     * @throws Fabric3Exception if no generator is registered for the resource type
     */
    <T extends ResourceReference> ResourceReferenceGenerator<T> getResourceReferenceGenerator(Class<T> clazz) throws Fabric3Exception;

    /**
     * Returns the {@link ResourceGenerator} for the resource type.
     *
     * @param clazz the resource type the generator handles
     * @return the resource generator
     * @throws Fabric3Exception if no generator is registered for the resource type
     */
    <T extends Resource> ResourceGenerator<T> getResourceGenerator(Class<T> clazz) throws Fabric3Exception;

    /**
     * Returns registered {@link InterceptorGenerator}s.
     *
     * @return interceptor generators
     */
    List<InterceptorGenerator> getInterceptorGenerators();

}

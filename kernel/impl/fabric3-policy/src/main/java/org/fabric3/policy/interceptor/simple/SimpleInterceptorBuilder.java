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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.policy.interceptor.simple;

import java.net.URI;

import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.container.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Builder for simple interceptors.
 */
public class SimpleInterceptorBuilder implements InterceptorBuilder<SimpleInterceptorDefinition> {
    private ClassLoaderRegistry registry;

    public SimpleInterceptorBuilder(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public Interceptor build(SimpleInterceptorDefinition definition) throws ContainerException {

        String className = definition.getInterceptorClass();
        URI classLoaderUri = definition.getPolicyClassLoaderId();
        ClassLoader loader = registry.getClassLoader(classLoaderUri);
        if (loader == null) {
            // this is really a programming error
            throw new SimpleInterceptorBuilderException("Interceptor classloader not found: " + classLoaderUri);
        }

        try {
            Class<Interceptor> interceptorClass = (Class<Interceptor>) loader.loadClass(className);
            return interceptorClass.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new SimpleInterceptorBuilderException("Unable load class: " + className, e);
        }

    }

}

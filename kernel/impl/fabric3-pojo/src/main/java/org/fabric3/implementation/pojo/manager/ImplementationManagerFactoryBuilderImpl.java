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
package org.fabric3.implementation.pojo.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds an {@link ImplementationManagerFactoryBuilder}.
 */
@EagerInit
public class ImplementationManagerFactoryBuilderImpl implements ImplementationManagerFactoryBuilder {
    private ReflectionFactory reflectionFactory;
    private ClassLoaderRegistry classLoaderRegistry;

    public ImplementationManagerFactoryBuilderImpl(@Reference ReflectionFactory reflectionFactory, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.reflectionFactory = reflectionFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public ImplementationManagerFactoryImpl build(ImplementationManagerDefinition definition) throws Fabric3Exception {
        Constructor<?> ctr = definition.getConstructor();

        Map<InjectionSite, Injectable> injectionSites = definition.getConstruction();
        Injectable[] cdiSources = new Injectable[ctr.getParameterTypes().length];
        for (Map.Entry<InjectionSite, Injectable> entry : injectionSites.entrySet()) {
            InjectionSite site = entry.getKey();
            Injectable injectable = entry.getValue();
            ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
            cdiSources[constructorSite.getParam()] = injectable;
        }
        for (int i = 0; i < cdiSources.length; i++) {
            if (cdiSources[i] == null) {
                String clazz = ctr.getName();
                throw new Fabric3Exception("No injection value for constructor parameter " + i + " in class " + clazz);
            }
        }

        LifecycleInvoker initInvoker = getInitInvoker(definition);
        LifecycleInvoker destroyInvoker = getDestroyInvoker(definition);

        Map<InjectionSite, Injectable> postConstruction = definition.getPostConstruction();
        List<Injectable> construction = Arrays.asList(cdiSources);
        boolean reinjectable = definition.isReinjectable();
        ClassLoader cl = classLoaderRegistry.getClassLoader(definition.getClassLoaderUri());
        return new ImplementationManagerFactoryImpl(ctr,
                                                    construction,
                                                    postConstruction,
                                                    initInvoker,
                                                    destroyInvoker,
                                                    reinjectable,
                                                    cl,
                                                    reflectionFactory);
    }

    private LifecycleInvoker getInitInvoker(ImplementationManagerDefinition definition) {
        LifecycleInvoker initInvoker = null;
        Method initMethod = definition.getInitMethod();
        if (initMethod != null) {
            initInvoker = reflectionFactory.createLifecycleInvoker(initMethod);
        }
        return initInvoker;
    }

    private LifecycleInvoker getDestroyInvoker(ImplementationManagerDefinition definition) {
        LifecycleInvoker destroyInvoker = null;
        Method destroyMethod = definition.getDestroyMethod();
        if (destroyMethod != null) {
            destroyInvoker = reflectionFactory.createLifecycleInvoker(destroyMethod);
        }
        return destroyInvoker;
    }

}

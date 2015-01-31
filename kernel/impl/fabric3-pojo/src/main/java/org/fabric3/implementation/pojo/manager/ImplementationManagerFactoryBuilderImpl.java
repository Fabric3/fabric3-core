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
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.api.model.type.java.Signature;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
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

    public ImplementationManagerFactoryImpl build(ImplementationManagerDefinition definition, ClassLoader cl) throws ContainerException {
        try {
            URI componentUri = definition.getComponentUri();
            String className = definition.getImplementationClass();
            Class<?> implClass = classLoaderRegistry.loadClass(cl, className);
            Constructor<?> ctr = getConstructor(implClass, definition.getConstructor());

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
                    throw new ContainerException("No injection value for constructor parameter " + i + " in class " + clazz);
                }
            }

            LifecycleInvoker initInvoker = getInitInvoker(definition, implClass);
            LifecycleInvoker destroyInvoker = getDestroyInvoker(definition, implClass);

            Map<InjectionSite, Injectable> postConstruction = definition.getPostConstruction();
            List<Injectable> construction = Arrays.asList(cdiSources);
            boolean reinjectable = definition.isReinjectable();

            return new ImplementationManagerFactoryImpl(componentUri,
                                                        ctr,
                                                        construction,
                                                        postConstruction,
                                                        initInvoker,
                                                        destroyInvoker,
                                                        reinjectable,
                                                        cl,
                                                        reflectionFactory);
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            throw new ContainerException(ex);
        }
    }

    private LifecycleInvoker getInitInvoker(ImplementationManagerDefinition definition, Class<?> implClass)
            throws NoSuchMethodException, ClassNotFoundException {
        LifecycleInvoker initInvoker = null;
        Method initMethod = getMethod(implClass, definition.getInitMethod());
        if (initMethod != null) {
            initInvoker = reflectionFactory.createLifecycleInvoker(initMethod);
        }
        return initInvoker;
    }

    private LifecycleInvoker getDestroyInvoker(ImplementationManagerDefinition definition, Class<?> implClass)
            throws NoSuchMethodException, ClassNotFoundException {
        LifecycleInvoker destroyInvoker = null;
        Method destroyMethod = getMethod(implClass, definition.getDestroyMethod());
        if (destroyMethod != null) {
            destroyInvoker = reflectionFactory.createLifecycleInvoker(destroyMethod);
        }
        return destroyInvoker;
    }

    private Method getMethod(Class<?> implClass, Signature signature) throws NoSuchMethodException, ClassNotFoundException {
        return signature == null ? null : signature.getMethod(implClass);
    }

    private <T> Constructor<T> getConstructor(Class<T> implClass, Signature signature) throws ClassNotFoundException, NoSuchMethodException {
        Constructor<T> ctr = signature.getConstructor(implClass);
        ctr.setAccessible(true);
        return ctr;
    }

}

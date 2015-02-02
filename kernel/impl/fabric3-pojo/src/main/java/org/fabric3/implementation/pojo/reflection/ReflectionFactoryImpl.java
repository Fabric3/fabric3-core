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
package org.fabric3.implementation.pojo.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.InjectorFactory;
import org.fabric3.implementation.pojo.spi.reflection.InstantiatorFactory;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvokerFactory;
import org.fabric3.spi.container.injection.Injector;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ReflectionFactoryImpl implements ReflectionFactory {

    private InstantiatorFactory instantiatorFactory;
    private InjectorFactory injectorFactory;
    private LifecycleInvokerFactory lifecycleInvokerFactory;
    private ServiceInvokerFactory serviceInvokerFactory;
    private ConsumerInvokerFactory consumerInvokerFactory;

    public ReflectionFactoryImpl(@Reference InstantiatorFactory instantiatorFactory,
                                 @Reference InjectorFactory injectorFactory,
                                 @Reference LifecycleInvokerFactory lifecycleInvokerFactory,
                                 @Reference ServiceInvokerFactory serviceInvokerFactory,
                                 @Reference ConsumerInvokerFactory consumerInvokerFactory) {
        this.instantiatorFactory = instantiatorFactory;
        this.injectorFactory = injectorFactory;
        this.lifecycleInvokerFactory = lifecycleInvokerFactory;
        this.serviceInvokerFactory = serviceInvokerFactory;
        this.consumerInvokerFactory = consumerInvokerFactory;
    }

    @Reference(required = false)
    public void setInstantiatorFactories(List<InstantiatorFactory> factories) {
        factories.stream().filter(factory -> !factory.isDefault() || instantiatorFactory == null).forEach(factory -> instantiatorFactory = factory);
    }

    @Reference(required = false)
    public void setInjectorFactories(List<InjectorFactory> factories) {
        factories.stream().filter(factory -> !factory.isDefault() || injectorFactory == null).forEach(factory -> injectorFactory = factory);
    }

    @Reference(required = false)
    public void setLifecycleInvokerFactories(List<LifecycleInvokerFactory> factories) {
        factories.stream().filter(factory -> !factory.isDefault() || lifecycleInvokerFactory == null).forEach(factory -> lifecycleInvokerFactory = factory);
    }

    @Reference(required = false)
    public void setServiceInvokerFactories(List<ServiceInvokerFactory> factories) {
        factories.stream().filter(factory -> !factory.isDefault() || serviceInvokerFactory == null).forEach(factory -> serviceInvokerFactory = factory);
    }

    @Reference(required = false)
    public void setConsumerInvokerFactories(List<ConsumerInvokerFactory> factories) {
        factories.stream().filter(factory -> !factory.isDefault() || serviceInvokerFactory == null).forEach(factory -> consumerInvokerFactory = factory);
    }

    public Supplier<?> createInstantiator(Constructor<?> constructor, Supplier<?>[] suppliers) {
        return instantiatorFactory.createInstantiator(constructor, suppliers);
    }

    public Injector<?> createInjector(Member member, Supplier<?> supplier) {
        return injectorFactory.createInjector(member, supplier);
    }

    public LifecycleInvoker createLifecycleInvoker(Method method) {
        return lifecycleInvokerFactory.createLifecycleInvoker(method);
    }

    public ServiceInvoker createServiceInvoker(Method method) {
        return serviceInvokerFactory.createInvoker(method);
    }

    public ConsumerInvoker createConsumerInvoker(Method method) {
        return consumerInvokerFactory.createInvoker(method);
    }
}

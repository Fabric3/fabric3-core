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

import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.InjectorFactory;
import org.fabric3.implementation.pojo.spi.reflection.InstantiatorFactory;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvoker;
import org.fabric3.implementation.pojo.spi.reflection.LifecycleInvokerFactory;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvokerFactory;
import org.fabric3.spi.container.objectfactory.Injector;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
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
        for (InstantiatorFactory factory : factories) {
            if (!factory.isDefault() || instantiatorFactory == null) {
                instantiatorFactory = factory;

            }
        }
    }

    @Reference(required = false)
    public void setInjectorFactories(List<InjectorFactory> factories) {
        for (InjectorFactory factory : factories) {
            if (!factory.isDefault() || injectorFactory == null) {
                injectorFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setLifecycleInvokerFactories(List<LifecycleInvokerFactory> factories) {
        for (LifecycleInvokerFactory factory : factories) {
            if (!factory.isDefault() || lifecycleInvokerFactory == null) {
                lifecycleInvokerFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setServiceInvokerFactories(List<ServiceInvokerFactory> factories) {
        for (ServiceInvokerFactory factory : factories) {
            if (!factory.isDefault() || serviceInvokerFactory == null) {
                serviceInvokerFactory = factory;
            }
        }
    }

    @Reference(required = false)
    public void setConsumerInvokerFactories(List<ConsumerInvokerFactory> factories) {
        for (ConsumerInvokerFactory factory : factories) {
            if (!factory.isDefault() || serviceInvokerFactory == null) {
                consumerInvokerFactory = factory;
            }
        }
    }

    public <T> ObjectFactory<T> createInstantiator(Constructor<T> constructor, ObjectFactory<?>[] parameterFactories) {
        return instantiatorFactory.createInstantiator(constructor, parameterFactories);
    }

    public Injector<?> createInjector(Member member, ObjectFactory<?> parameterFactory) {
        return injectorFactory.createInjector(member, parameterFactory);
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

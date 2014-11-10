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
package org.fabric3.implementation.junit.runtime;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.implementation.junit.provision.JUnitWireTargetDefinition;
import org.fabric3.implementation.pojo.builder.MethodUtils;
import org.fabric3.implementation.pojo.component.InvokerInterceptor;
import org.fabric3.implementation.pojo.provision.PojoWireSourceDefinition;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.pojo.spi.reflection.ServiceInvoker;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.junit.Test;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches wires from Java components.
 */
public class JUnitTargetWireAttacher implements TargetWireAttacher<JUnitWireTargetDefinition> {

    private final ComponentManager manager;
    private ReflectionFactory reflectionFactory;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JUnitTargetWireAttacher(@Reference ComponentManager manager,
                                   @Reference ReflectionFactory reflectionFactory,
                                   @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.reflectionFactory = reflectionFactory;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalWireSourceDefinition sourceDefinition, JUnitWireTargetDefinition targetDefinition, Wire wire) throws ContainerException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        if (component == null) {
            throw new ContainerException("Target not found: " + targetName);
        }
        JavaComponent target = (JavaComponent) component;

        Class<?> implementationClass = target.getImplementationClass();
        ClassLoader loader = classLoaderRegistry.getClassLoader(targetDefinition.getClassLoaderId());

        // attach the invoker interceptor to forward invocation chains
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            Method method = MethodUtils.findMethod(sourceDefinition, targetDefinition, operation, implementationClass, loader, classLoaderRegistry);
            ServiceInvoker invoker = reflectionFactory.createServiceInvoker(method);
            InvokerInterceptor interceptor;
            if (sourceDefinition instanceof PojoWireSourceDefinition && targetDefinition.getClassLoaderId().equals(sourceDefinition.getClassLoaderId())) {
                // if the source is Java and target classloaders are equal, do not set the TCCL
                interceptor = new InvokerInterceptor(invoker, target);
            } else {
                // If the source and target classloaders are not equal, configure the interceptor to set the TCCL to the target classloader
                // when dispatching to a target instance. This guarantees when application code executes, it does so with the TCCL set to the
                // target component's classloader.
                interceptor = new InvokerInterceptor(invoker, target, loader);
            }
            Test annotation = method.getAnnotation(Test.class);
            if (annotation != null) {
                Class<? extends Throwable> expected = annotation.expected();
                if (!expected.equals(Test.None.class)) {
                    ExpectedExceptionInterceptor exceptionInterceptor = new ExpectedExceptionInterceptor(expected);
                    chain.addInterceptor(exceptionInterceptor);
                }
            }
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, JUnitWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JUnitWireTargetDefinition target) throws ContainerException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        JavaComponent targetComponent = (JavaComponent) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }

}
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
package org.fabric3.implementation.spring.runtime.builder;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.implementation.spring.provision.SpringWireTargetDefinition;
import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.pojo.builder.MethodUtils;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.implementation.spring.runtime.component.SpringInvoker;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spring.spi.WireListener;

/**
 * Attaches the target side of a wire to a Spring component.
 */
@EagerInit
public class SpringTargetWireAttacher implements TargetWireAttacher<SpringWireTargetDefinition> {
    private ComponentManager manager;
    private ClassLoaderRegistry classLoaderRegistry;
    private List<WireListener> listeners = Collections.emptyList();

    public SpringTargetWireAttacher(@Reference ComponentManager manager, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference(required = false)
    public void setListeners(List<WireListener> listeners) {
        this.listeners = listeners;
    }

    public void attach(PhysicalWireSourceDefinition source, SpringWireTargetDefinition target, Wire wire) throws ContainerException {
        String beanName = target.getBeanName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(target.getBeanInterface());
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
        for (WireListener listener : listeners) {
            listener.onAttach(wire);
        }
        SpringComponent component = getComponent(target);
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            Method beanMethod = MethodUtils.findMethod(source, target, operation, interfaze, loader, classLoaderRegistry);
            SpringInvoker invoker = new SpringInvoker(beanName, beanMethod, component);
            chain.addInterceptor(invoker);
        }
    }

    public void detach(PhysicalWireSourceDefinition source, SpringWireTargetDefinition target) throws ContainerException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(SpringWireTargetDefinition target) throws ContainerException {
        throw new UnsupportedOperationException();
    }

    private SpringComponent getComponent(SpringWireTargetDefinition definition) throws ContainerException {
        URI uri = UriHelper.getDefragmentedName(definition.getUri());
        SpringComponent component = (SpringComponent) manager.getComponent(uri);
        if (component == null) {
            throw new ContainerException("Target not found: " + uri);
        }
        return component;
    }

}

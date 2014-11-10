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
package org.fabric3.node.nonmanaged;

import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches a wire or object factory to non-managed code. The attachment is done by generating a proxy or returning an object instance which is then set on the
 * physical source definition. The proxy or instance can then be returned to the non-managed code.
 */
@EagerInit
public class NonManagedComponentSourceWireAttacher implements SourceWireAttacher<NonManagedPhysicalWireSourceDefinition> {
    private WireProxyService proxyService;
    private ClassLoaderRegistry registry;

    public NonManagedComponentSourceWireAttacher(@Reference WireProxyService proxyService, @Reference ClassLoaderRegistry registry) {
        this.proxyService = proxyService;
        this.registry = registry;
    }

    public void attach(NonManagedPhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        try {
            ClassLoader loader = registry.getClassLoader(source.getClassLoaderId());
            Class<?> interfaze = loader.loadClass(source.getInterface());
            Object proxy = proxyService.createObjectFactory(interfaze, wire, null).getInstance();
            source.setProxy(proxy);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    public void attachObjectFactory(NonManagedPhysicalWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws ContainerException {
        source.setProxy(objectFactory.getInstance());
    }

    public void detach(NonManagedPhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
    }

    public void detachObjectFactory(NonManagedPhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
    }
}

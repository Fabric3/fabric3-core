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

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.spring.provision.SpringWireSourceDefinition;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spring.spi.WireListener;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches the source side of a wire to a Spring component.
 */
@EagerInit
public class SpringSourceWireAttacher implements SourceWireAttacher<SpringWireSourceDefinition> {
    private ComponentManager manager;
    private WireProxyService proxyService;

    private ClassLoaderRegistry classLoaderRegistry;
    private List<WireListener> listeners = Collections.emptyList();

    public SpringSourceWireAttacher(@Reference ComponentManager manager,
                                    @Reference WireProxyService proxyService,
                                    @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
        this.proxyService = proxyService;
    }

    @Reference(required = false)
    public void setListeners(List<WireListener> listeners) {
        this.listeners = listeners;
    }

    public void attach(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(source.getInterface());
            // note callbacks not supported for spring beans
            ObjectFactory<?> factory = proxyService.createObjectFactory(interfaze, wire, null);
            component.attach(referenceName, interfaze, factory);
            for (WireListener listener : listeners) {
                listener.onAttach(wire);
            }
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    public void attachObjectFactory(SpringWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws ContainerException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        ClassLoader loader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        Class<?> interfaze;
        try {
            interfaze = loader.loadClass(source.getInterface());
            component.attach(referenceName, interfaze, objectFactory);
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
    }

    public void detach(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        SpringComponent component = getComponent(source);
        String referenceName = source.getReferenceName();
        component.detach(referenceName);
    }

    public void detachObjectFactory(SpringWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        detach(source, target);
    }

    private SpringComponent getComponent(SpringWireSourceDefinition definition) throws ContainerException {
        URI uri = definition.getUri();
        SpringComponent component = (SpringComponent) manager.getComponent(uri);
        if (component == null) {
            throw new ContainerException("Source not found: " + uri);
        }
        return component;
    }


}

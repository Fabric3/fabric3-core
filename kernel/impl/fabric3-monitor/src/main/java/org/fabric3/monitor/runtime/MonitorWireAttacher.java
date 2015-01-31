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
package org.fabric3.monitor.runtime;

import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.monitor.provision.MonitorWireTargetDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * TargetWireAttacher that handles monitor resources.
 */
public class MonitorWireAttacher implements TargetWireAttacher<MonitorWireTargetDefinition> {
    private final MonitorProxyService monitorService;
    private ComponentManager componentManager;
    private final ClassLoaderRegistry classLoaderRegistry;

    public MonitorWireAttacher(@Reference MonitorProxyService monitorService,
                               @Reference ComponentManager componentManager,
                               @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.monitorService = monitorService;
        this.componentManager = componentManager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target, Wire wire) throws ContainerException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target) throws ContainerException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(MonitorWireTargetDefinition target) throws ContainerException {
        try {
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Class<?> type = classLoaderRegistry.loadClass(loader, target.getMonitorType());
            Component monitorable = componentManager.getComponent(target.getMonitorable());
            Object monitor = monitorService.createMonitor(type, monitorable, target.getDestination());
            return new SingletonObjectFactory<>(monitor);
        } catch (ClassNotFoundException e) {
            throw new ContainerException("Unable to load monitor class: " + target.getMonitorType(), e);
        } catch (MonitorCreationException e) {
            throw new ContainerException("Unable to create monitor for class: " + target.getMonitorType(), e);
        }
    }
}

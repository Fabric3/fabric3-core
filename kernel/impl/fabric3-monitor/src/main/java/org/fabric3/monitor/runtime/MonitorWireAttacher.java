/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.monitor.runtime;

import org.fabric3.monitor.provision.MonitorWireTargetDefinition;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.component.TargetWireAttacher;
import org.fabric3.spi.container.builder.component.AttachException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.container.wire.Wire;

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

    public void attach(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target, Wire wire) throws BuilderException {
        throw new UnsupportedOperationException();
    }

    public void detach(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target) throws BuilderException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(MonitorWireTargetDefinition target) throws BuilderException {
        try {
            ClassLoader loader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
            Class<?> type = classLoaderRegistry.loadClass(loader, target.getMonitorType());
            Component monitorable = componentManager.getComponent(target.getMonitorable());
            Object monitor = monitorService.createMonitor(type, monitorable, target.getDestination());
            return new SingletonObjectFactory<>(monitor);
        } catch (ClassNotFoundException e) {
            throw new AttachException("Unable to load monitor class: " + target.getMonitorType(), e);
        } catch (MonitorCreationException e) {
            throw new AttachException("Unable to create monitor for class: " + target.getMonitorType(), e);
        }
    }
}

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
package org.fabric3.fabric.runtime;

import javax.management.MBeanServer;

import org.fabric3.host.monitor.DestinationRouter;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.repository.Repository;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.management.ManagementService;

/**
 * Interface for accessing services provided by a runtime.
 * <p/>
 * These are the primordial services that should be provided by all runtime implementations for use by other runtime components.
 */
public interface RuntimeServices {

    /**
     * Returns the info this runtime will make available to service components.
     *
     * @return the info this runtime will make available to service components
     */
    HostInfo getHostInfo();

    /**
     * Returns the {@link MonitorProxyService} that this runtime is using.
     *
     * @return the {@link MonitorProxyService} that this runtime is using
     */
    MonitorProxyService getMonitorProxyService();

    /**
     * Returns the runtime management service.
     *
     * @return the MBeanServer
     */
    ManagementService getManagementService();

    /**
     * Returns the runtime MBeanServer.
     *
     * @return the MBeanServer
     */
    MBeanServer getMBeanServer();

    /**
     * Returns this runtime's logical component manager.
     *
     * @return this runtime's logical component manager
     */
    LogicalComponentManager getLogicalComponentManager();

    /**
     * Returns this runtime's physical component manager.
     *
     * @return this runtime's physical component manager
     */
    ComponentManager getComponentManager();

    /**
     * Returns this runtime's channel manager.
     *
     * @return this runtime's channel manager
     */
    ChannelManager getChannelManager();

    /**
     * Returns the ScopeRegistry used to manage runtime ScopeContainers.
     *
     * @return the ScopeRegistry used to manage runtime ScopeContainers
     */
    ScopeRegistry getScopeRegistry();

    /**
     * Returns the ScopeContainer used to manage runtime component instances.
     *
     * @return the ScopeContainer used to manage runtime component instances
     */
    ScopeContainer getScopeContainer();

    /**
     * Returns the ClassLoaderRegistry used to manage runtime classloaders.
     *
     * @return the ClassLoaderRegistry used to manage runtime classloaders
     */
    ClassLoaderRegistry getClassLoaderRegistry();

    /**
     * Returns the MetaDataStore used to index contribution resources.
     *
     * @return the MetaDataStore used to index contribution resources
     */
    MetaDataStore getMetaDataStore();

    /**
     * Returns the runtime repository.
     *
     * @return the runtime repository.
     */
    Repository getRepository();

    /**
     * Returns the default monitor destination router.
     */
    DestinationRouter getDestinationRouter();
}

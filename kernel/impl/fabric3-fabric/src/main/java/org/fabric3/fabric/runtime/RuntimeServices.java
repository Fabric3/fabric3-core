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
package org.fabric3.fabric.runtime;

import javax.management.MBeanServer;

import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.management.ManagementService;

/**
 * Interface for accessing services provided by a runtime.
 *
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
     * Returns the runtime logical component manager.
     *
     * @return the runtime logical component manager
     */
    LogicalComponentManager getLogicalComponentManager();

    /**
     * Returns the runtime component manager.
     *
     * @return the runtime component manager
     */
    ComponentManager getComponentManager();

    /**
     * Returns the runtime channel manager.
     *
     * @return the runtime channel manager
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
     * Returns the default monitor destination router.
     */
    DestinationRouter getDestinationRouter();
}

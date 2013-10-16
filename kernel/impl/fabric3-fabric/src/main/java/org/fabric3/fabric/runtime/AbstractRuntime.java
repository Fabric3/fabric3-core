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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.runtime;

import javax.management.MBeanServer;
import java.io.File;
import java.net.URI;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.contribution.MetaDataStoreImpl;
import org.fabric3.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.container.channel.ChannelManagerImpl;
import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;
import org.fabric3.fabric.container.component.ComponentManagerImpl;
import org.fabric3.fabric.container.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.container.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.container.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.domain.LogicalComponentManagerImpl;
import org.fabric3.fabric.management.DelegatingManagementService;
import org.fabric3.fabric.repository.RepositoryImpl;
import org.fabric3.api.host.Names;
import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.monitor.MonitorCreationException;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.api.host.repository.Repository;
import org.fabric3.api.host.repository.RepositoryException;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.InitializationException;
import org.fabric3.api.host.runtime.RuntimeConfiguration;
import org.fabric3.api.host.runtime.ShutdownException;
import org.fabric3.monitor.proxy.JDKMonitorProxyService;
import org.fabric3.monitor.proxy.MonitorProxyServiceImpl;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.management.ManagementService;

/**
 *
 */
public abstract class AbstractRuntime implements Fabric3Runtime, RuntimeServices {
    private static final String JAVA_LIBRARY_PATH = "java.library.path";

    private HostInfo hostInfo;
    private MonitorProxyService monitorService;
    private LogicalComponentManager logicalComponentManager;
    private ComponentManager componentManager;
    private ChannelManager channelManager;
    private CompositeScopeContainer scopeContainer;
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore metaDataStore;
    private ScopeRegistry scopeRegistry;
    private ManagementService managementService;
    private MBeanServer mbServer;
    private Repository repository;
    private MonitorLevel level = MonitorLevel.INFO;
    private DestinationRouter router;

    protected AbstractRuntime(RuntimeConfiguration configuration) {
        hostInfo = configuration.getHostInfo();
        mbServer = configuration.getMBeanServer();
        router = configuration.getDestinationRouter();
        repository = configuration.getRepository();
        System.setProperty(JAVA_LIBRARY_PATH, new File(hostInfo.getTempDir(), "native").getAbsolutePath());
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public MonitorProxyService getMonitorProxyService() {
        return monitorService;
    }

    public MBeanServer getMBeanServer() {
        return mbServer;
    }

    public String getName() {
        return Names.RUNTIME_NAME;
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    public void boot() throws InitializationException {
        logicalComponentManager = new LogicalComponentManagerImpl();
        componentManager = new ComponentManagerImpl();
        channelManager = new ChannelManagerImpl();

        classLoaderRegistry = new ClassLoaderRegistryImpl();
        ProcessorRegistry processorRegistry = new ProcessorRegistryImpl();
        metaDataStore = new MetaDataStoreImpl(processorRegistry);

        monitorService = new MonitorProxyServiceImpl(new JDKMonitorProxyService(this, router));

        ScopeContainerMonitor monitor;
        try {
            monitor = monitorService.createMonitor(ScopeContainerMonitor.class);
        } catch (MonitorCreationException e) {
            throw new InitializationException(e);
        }
        scopeContainer = new CompositeScopeContainer(monitor);
        scopeContainer.start();
        scopeRegistry = new ScopeRegistryImpl();
        scopeRegistry.register(scopeContainer);
        managementService = new DelegatingManagementService();
        if (repository == null) {
            // if the runtime has not been configured with a repository, create one
            repository = createRepository();
        }
    }

    public void destroy() throws ShutdownException {
        // destroy system components
        WorkContextCache.getAndResetThreadWorkContext();
        scopeContainer.stopAllContexts();
        try {
            repository.shutdown();
        } catch (RepositoryException e) {
            throw new ShutdownException(e);
        }
    }

    public <I> I getComponent(Class<I> service, URI uri) {
        if (RuntimeServices.class.equals(service)) {
            return service.cast(this);
        }
        ScopedComponent component = (ScopedComponent) componentManager.getComponent(uri);
        if (component == null) {
            return null;
        }

        try {
            Object instance = component.getInstance();
            return service.cast(instance);
        } catch (InstanceLifecycleException e) {
            // this is an error with the runtime and not something that is recoverable
            throw new AssertionError(e);
        }
    }

    public <I> I getComponent(Class<I> service) {
        return getComponent(service, URI.create(Names.RUNTIME_NAME + "/" + service.getSimpleName()));
    }

    public LogicalComponentManager getLogicalComponentManager() {
        return logicalComponentManager;
    }

    public ComponentManager getComponentManager() {
        return componentManager;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ScopeContainer getScopeContainer() {
        return scopeContainer;
    }

    public ClassLoaderRegistry getClassLoaderRegistry() {
        return classLoaderRegistry;
    }

    public MetaDataStore getMetaDataStore() {
        return metaDataStore;
    }

    public ScopeRegistry getScopeRegistry() {
        return scopeRegistry;
    }

    public ManagementService getManagementService() {
        return managementService;
    }

    public Repository getRepository() {
        return repository;
    }

    public DestinationRouter getDestinationRouter() {
        return router;
    }

    /**
     * Creates a default repository which may be overridden by subclasses.
     *
     * @return an initialized repository
     * @throws InitializationException if an error is encountered initializing a repository
     */
    protected Repository createRepository() throws InitializationException {
        try {
            RepositoryImpl repository = new RepositoryImpl(hostInfo);
            repository.init();
            return repository;
        } catch (RepositoryException e) {
            throw new InitializationException(e);
        }
    }

}

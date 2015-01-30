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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.runtime;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.fabric3.api.annotation.monitor.MonitorLevel;
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
import org.fabric3.contribution.MetaDataStoreImpl;
import org.fabric3.contribution.ProcessorRegistryImpl;
import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;
import org.fabric3.fabric.container.channel.ChannelManagerImpl;
import org.fabric3.fabric.container.component.ComponentManagerImpl;
import org.fabric3.fabric.container.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.container.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.container.component.scope.ScopeRegistryImpl;
import org.fabric3.fabric.domain.LogicalComponentManagerImpl;
import org.fabric3.fabric.management.DelegatingManagementService;
import org.fabric3.fabric.repository.RepositoryImpl;
import org.fabric3.monitor.proxy.JDKMonitorProxyService;
import org.fabric3.monitor.proxy.MonitorProxyServiceImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.component.ScopedComponent;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
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
    private DestinationRouter router;
    private MonitorLevel level = MonitorLevel.INFO;

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
            classLoaderRegistry.close();
        } catch (RepositoryException | IOException e) {
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
        } catch (ContainerException e) {
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

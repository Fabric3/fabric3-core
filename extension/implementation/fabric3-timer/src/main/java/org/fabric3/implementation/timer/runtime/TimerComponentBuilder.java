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
package org.fabric3.implementation.timer.runtime;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.timer.provision.TimerComponentDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.fabric3.timer.spi.TimerService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class TimerComponentBuilder extends PojoComponentBuilder<TimerComponentDefinition, TimerComponent> implements Fabric3EventListener<RuntimeStart> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;
    private TimerService timerService;
    private TransactionManager tm;
    private HostInfo info;
    private NodeTopologyService topologyService;
    private InvokerMonitor monitor;

    private List<TimerComponent> scheduleQueue;
    private boolean runtimeStarted;

    public TimerComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                 @Reference ImplementationManagerFactoryBuilder factoryBuilder,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference PropertyObjectFactoryBuilder propertyBuilder,
                                 @Reference TimerService timerService,
                                 @Reference TransactionManager tm,
                                 @Reference ManagementService managementService,
                                 @Reference IntrospectionHelper helper,
                                 @Reference EventService eventService,
                                 @Reference HostInfo info,
                                 @Monitor InvokerMonitor monitor) {
        super(classLoaderRegistry, propertyBuilder, managementService, helper, info);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
        this.timerService = timerService;
        this.tm = tm;
        this.info = info;
        this.monitor = monitor;
        eventService.subscribe(RuntimeStart.class, this);
        scheduleQueue = new ArrayList<>();
    }

    @Reference(required = false)
    public void setTopologyService(NodeTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public TimerComponent build(TimerComponentDefinition definition) throws ContainerException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        ImplementationManagerDefinition managerDefinition = definition.getFactoryDefinition();
        Class<?> implClass;
        try {
            implClass = classLoader.loadClass(managerDefinition.getImplementationClass());
        } catch (ClassNotFoundException e) {
            throw new ContainerException(e);
        }
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition, classLoader);

        createPropertyFactories(definition, factory);
        TimerData data = definition.getTriggerData();
        boolean transactional = definition.isTransactional();
        TimerComponent component = new TimerComponent(uri,
                                                      deployable,
                                                      data,
                                                      implClass,
                                                      transactional,
                                                      factory,
                                                      scopeContainer,
                                                      timerService,
                                                      tm,
                                                      topologyService,
                                                      info,
                                                      monitor,
                                                      runtimeStarted);
        if (!runtimeStarted) {
            // defer scheduling to after the runtime has started
            scheduleQueue.add(component);
        }
        buildContexts(component, factory);
        export(definition, classLoader, component);
        return component;
    }

    public void dispose(TimerComponentDefinition definition, TimerComponent component) throws ContainerException {
        dispose(definition);
    }

    public void onEvent(RuntimeStart event) {
        // runtime has started, schedule any deferred components
        runtimeStarted = true;
        for (TimerComponent component : scheduleQueue) {
            component.schedule();
        }
        scheduleQueue.clear();
    }
}

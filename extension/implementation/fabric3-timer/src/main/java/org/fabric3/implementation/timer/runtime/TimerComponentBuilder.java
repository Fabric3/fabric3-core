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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertySupplierBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.timer.provision.TimerPhysicalComponent;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.discovery.DiscoveryAgent;
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
public class TimerComponentBuilder extends PojoComponentBuilder<TimerPhysicalComponent, TimerComponent> implements Fabric3EventListener<RuntimeStart> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;
    private TimerService timerService;
    private TransactionManager tm;
    private HostInfo info;
    private InvokerMonitor monitor;

    private List<TimerComponent> scheduleQueue;
    private boolean runtimeStarted;

    @Reference(required = false)
    protected DiscoveryAgent discoveryAgent;

    public TimerComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                 @Reference ImplementationManagerFactoryBuilder factoryBuilder,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference PropertySupplierBuilder propertyBuilder,
                                 @Reference TimerService timerService,
                                 @Reference TransactionManager tm,
                                 @Reference ManagementService managementService,
                                 @Reference IntrospectionHelper helper,
                                 @Reference EventService eventService,
                                 @Reference HostInfo info,
                                 @Monitor InvokerMonitor monitor) {
        super(propertyBuilder, managementService, helper, info);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
        this.timerService = timerService;
        this.tm = tm;
        this.info = info;
        this.monitor = monitor;
        eventService.subscribe(RuntimeStart.class, this);
        scheduleQueue = new ArrayList<>();
    }

    public TimerComponent build(TimerPhysicalComponent physicalComponent) {
        URI uri = physicalComponent.getComponentUri();

        Scope scopeName = physicalComponent.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        ImplementationManagerDefinition managerDefinition = physicalComponent.getFactoryDefinition();
        Class<?> implClass = managerDefinition.getImplementationClass();
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition);

        createPropertyFactories(physicalComponent, factory);
        TimerData data = physicalComponent.getTriggerData();
        boolean transactional = physicalComponent.isTransactional();
        URI contributionUri = physicalComponent.getContributionUri();
        TimerComponent component = new TimerComponent(uri,
                                                      data,
                                                      implClass,
                                                      transactional,
                                                      factory,
                                                      scopeContainer,
                                                      timerService,
                                                      tm,
                                                      discoveryAgent,
                                                      info,
                                                      monitor,
                                                      runtimeStarted,
                                                      contributionUri);
        if (!runtimeStarted) {
            // defer scheduling to after the runtime has started
            scheduleQueue.add(component);
        }
        buildContexts(component, factory);
        export(physicalComponent, component);
        return component;
    }

    public void dispose(TimerPhysicalComponent physicalComponent, TimerComponent component) {
        dispose(physicalComponent);
    }

    public void onEvent(RuntimeStart event) {
        // runtime has started, schedule any deferred components
        runtimeStarted = true;
        scheduleQueue.forEach(TimerComponent::schedule);
        scheduleQueue.clear();
    }
}

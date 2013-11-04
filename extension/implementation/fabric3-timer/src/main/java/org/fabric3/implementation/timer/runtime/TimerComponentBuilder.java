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
package org.fabric3.implementation.timer.runtime;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.timer.provision.TimerComponentDefinition;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStart;
import org.fabric3.spi.federation.topology.ParticipantTopologyService;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
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
    private ParticipantTopologyService topologyService;
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
        scheduleQueue = new ArrayList<TimerComponent>();
    }

    @Reference(required = false)
    public void setTopologyService(ParticipantTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public TimerComponent build(TimerComponentDefinition definition) throws BuilderException {
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
            throw new BuilderException(e);
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

    public void dispose(TimerComponentDefinition definition, TimerComponent component) throws BuilderException {
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

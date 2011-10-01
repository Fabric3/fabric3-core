/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.net.URI;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.implementation.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.implementation.timer.provision.TimerComponentDefinition;
import org.fabric3.implementation.timer.provision.TimerData;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.timer.spi.TimerService;

/**
 * @version $Rev: 7712 $ $Date: 2009-09-29 00:57:34 +0200 (Tue, 29 Sep 2009) $
 */
@EagerInit
public class TimerComponentBuilder extends PojoComponentBuilder<TimerComponentDefinition, TimerComponent> {
    private ScopeRegistry scopeRegistry;
    private InstanceFactoryBuilder factoryBuilder;
    private TimerService timerService;
    private TransactionManager tm;
    private HostInfo info;
    private ZoneTopologyService topologyService;
    private InvokerMonitor monitor;

    public TimerComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                 @Reference InstanceFactoryBuilder factoryBuilder,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference PropertyObjectFactoryBuilder propertyBuilder,
                                 @Reference TimerService timerService,
                                 @Reference TransactionManager tm,
                                 @Reference ManagementService managementService,
                                 @Reference IntrospectionHelper helper,
                                 @Reference HostInfo info,
                                 @Monitor InvokerMonitor monitor) {
        super(classLoaderRegistry, propertyBuilder, managementService, helper);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
        this.timerService = timerService;
        this.tm = tm;
        this.info = info;
        this.monitor = monitor;
    }

    @Reference(required = false)
    public void setTopologyService(ZoneTopologyService topologyService) {
        this.topologyService = topologyService;
    }

    public TimerComponent build(TimerComponentDefinition definition) throws BuilderException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        InstanceFactoryDefinition factoryDefinition = definition.getFactoryDefinition();
        Class<?> implClass;
        try {
            implClass = classLoader.loadClass(factoryDefinition.getImplementationClass());
        } catch (ClassNotFoundException e) {
            throw new BuilderException(e);
        }
        InstanceFactoryProvider provider = factoryBuilder.build(factoryDefinition, classLoader);

        createPropertyFactories(definition, provider);
        TimerData data = definition.getTriggerData();
        boolean transactional = definition.isTransactional();
        TimerComponent component = new TimerComponent(uri,
                                                      deployable,
                                                      data,
                                                      implClass,
                                                      transactional,
                                                      provider,
                                                      scopeContainer,
                                                      timerService,
                                                      tm,
                                                      topologyService,
                                                      info,
                                                      monitor);
        buildContexts(component, provider);
        export(definition, classLoader, component);
        return component;
    }

    public void dispose(TimerComponentDefinition definition, TimerComponent component) throws BuilderException {
        dispose(definition);
    }
}

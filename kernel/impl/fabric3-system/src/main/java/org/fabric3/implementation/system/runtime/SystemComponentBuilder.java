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
package org.fabric3.implementation.system.runtime;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class SystemComponentBuilder extends PojoComponentBuilder<SystemComponentDefinition, SystemComponent> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;

    public SystemComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                  @Reference ImplementationManagerFactoryBuilder factoryBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference PropertyObjectFactoryBuilder propertyBuilder,
                                  @Reference ManagementService managementService,
                                  @Reference IntrospectionHelper helper,
                                  @Reference HostInfo info) {
        super(classLoaderRegistry, propertyBuilder, managementService, helper, info);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
    }

    public SystemComponent build(SystemComponentDefinition definition) throws ContainerException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);

        // create the InstanceFactoryProvider based on the definition in the model
        ImplementationManagerDefinition managerDefinition = definition.getFactoryDefinition();
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition, classLoader);

        createPropertyFactories(definition, factory);

        boolean eager = definition.isEagerInit();
        SystemComponent component = new SystemComponent(uri, factory, scopeContainer, deployable, eager);
        export(definition, classLoader, component);
        return component;
    }

    public void dispose(SystemComponentDefinition definition, SystemComponent component) throws ContainerException {
        dispose(definition);
    }


}

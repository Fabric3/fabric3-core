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
package org.fabric3.implementation.java.runtime;

import java.net.URI;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertySupplierBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds a Java component from a physical definition.
 */
@EagerInit
public class JavaComponentBuilder extends PojoComponentBuilder<PhysicalJavaComponent, JavaComponent> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;

    public JavaComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                @Reference ImplementationManagerFactoryBuilder factoryBuilder,
                                @Reference PropertySupplierBuilder propertyBuilder,
                                @Reference ManagementService managementService,
                                @Reference IntrospectionHelper helper,
                                @Reference HostInfo info) {
        super(propertyBuilder, managementService, helper, info);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
    }

    public JavaComponent build(PhysicalJavaComponent physicalComponent) {
        return physicalComponent.getInstance() != null ? buildNonManagedComponent(physicalComponent) : buildManagedComponent(physicalComponent);
    }

    public void dispose(PhysicalJavaComponent physicalComponent, JavaComponent component) {
        dispose(physicalComponent);
    }

    private JavaComponent buildManagedComponent(PhysicalJavaComponent physicalComponent) {
        URI uri = physicalComponent.getComponentUri();

        // get the scope container for this component
        Scope scopeName = physicalComponent.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        // create the InstanceFactoryProvider based on the definition in the model
        ImplementationManagerDefinition managerDefinition = physicalComponent.getFactoryDefinition();
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition);

        createPropertyFactories(physicalComponent, factory);

        boolean eager = physicalComponent.isEagerInit();

        URI contributionUri = physicalComponent.getContributionUri();
        JavaComponent component = new JavaComponent(uri, factory, scopeContainer, eager, contributionUri);
        buildContexts(component, factory);
        export(physicalComponent, component);
        return component;
    }

    private JavaComponent buildNonManagedComponent(PhysicalJavaComponent physicalComponent) {
        URI componentUri = physicalComponent.getComponentUri();
        Scope scopeName = physicalComponent.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);
        Object instance = physicalComponent.getInstance();
        NonManagedImplementationManagerFactory factory = new NonManagedImplementationManagerFactory(instance);
        URI contributionUri = physicalComponent.getContributionUri();
        return new JavaComponent(componentUri, factory, scopeContainer, false, contributionUri);
    }

}

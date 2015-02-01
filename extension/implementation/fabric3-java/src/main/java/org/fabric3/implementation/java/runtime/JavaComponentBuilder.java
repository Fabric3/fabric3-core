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

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilder;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
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
public class JavaComponentBuilder extends PojoComponentBuilder<JavaComponentDefinition, JavaComponent> {
    private ScopeRegistry scopeRegistry;
    private ImplementationManagerFactoryBuilder factoryBuilder;

    public JavaComponentBuilder(@Reference ScopeRegistry scopeRegistry,
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

    public JavaComponent build(JavaComponentDefinition definition)  {
        if (definition.getInstance() != null) {
            return buildNonManagedComponent(definition);
        } else {
            return buildManagedComponent(definition);
        }
    }

    public void dispose(JavaComponentDefinition definition, JavaComponent component)  {
        dispose(definition);
    }

    private JavaComponent buildManagedComponent(JavaComponentDefinition definition)  {
        URI uri = definition.getComponentUri();

        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);

        // create the InstanceFactoryProvider based on the definition in the model
        ImplementationManagerDefinition managerDefinition = definition.getFactoryDefinition();
        ImplementationManagerFactory factory = factoryBuilder.build(managerDefinition, classLoader);

        createPropertyFactories(definition, factory);

        boolean eager = definition.isEagerInit();

        JavaComponent component = new JavaComponent(uri, factory, scopeContainer, deployable, eager);
        buildContexts(component, factory);
        export(definition, classLoader, component);
        return component;
    }

    private JavaComponent buildNonManagedComponent(JavaComponentDefinition definition) {
        URI componentUri = definition.getComponentUri();
        String scopeName = definition.getScope();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scopeName);
        Object instance = definition.getInstance();
        NonManagedImplementationManagerFactory factory = new NonManagedImplementationManagerFactory(instance);
        return new JavaComponent(componentUri, factory, scopeContainer, definition.getDeployable(), false);
    }

}

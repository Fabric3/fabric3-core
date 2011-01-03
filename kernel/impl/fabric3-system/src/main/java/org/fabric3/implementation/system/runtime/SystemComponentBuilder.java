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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.system.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.pojo.builder.PojoComponentBuilder;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilder;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.implementation.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.management.ManagementService;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemComponentBuilder extends PojoComponentBuilder<SystemComponentDefinition, SystemComponent> {
    private ScopeRegistry scopeRegistry;
    private InstanceFactoryBuilder factoryBuilder;

    public SystemComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                  @Reference InstanceFactoryBuilder factoryBuilder,
                                  @Reference ClassLoaderRegistry classLoaderRegistry,
                                  @Reference PropertyObjectFactoryBuilder propertyBuilder,
                                  @Reference ManagementService managementService,
                                  @Reference IntrospectionHelper helper) {
        super(classLoaderRegistry, propertyBuilder, managementService, helper);
        this.scopeRegistry = scopeRegistry;
        this.factoryBuilder = factoryBuilder;
    }

    public SystemComponent build(SystemComponentDefinition definition) throws BuilderException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition factoryDefinition = definition.getFactoryDefinition();
        InstanceFactoryProvider provider = factoryBuilder.build(factoryDefinition, classLoader);

        createPropertyFactories(definition, provider);

        boolean eager = definition.isEagerInit();
        SystemComponent component = new SystemComponent(uri, provider, scopeContainer, deployable, eager, -1, -1);
        export(definition, classLoader, component);
        return component;
    }

    public void dispose(SystemComponentDefinition definition, SystemComponent component) throws BuilderException {
        dispose(definition);
    }


}

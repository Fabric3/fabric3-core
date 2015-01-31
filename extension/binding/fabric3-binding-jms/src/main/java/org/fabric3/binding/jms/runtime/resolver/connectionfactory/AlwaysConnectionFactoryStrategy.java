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
package org.fabric3.binding.jms.runtime.resolver.connectionfactory;

import javax.jms.ConnectionFactory;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.runtime.resolver.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.api.host.ContainerException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that always attempts to create a connection factory.
 */
public class AlwaysConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private ConnectionFactoryCreatorRegistry creatorRegistry;
    private ConnectionFactoryManager manager;
    private Set<String> created = new HashSet<>();

    public AlwaysConnectionFactoryStrategy(@Reference ConnectionFactoryCreatorRegistry creatorRegistry, @Reference ConnectionFactoryManager manager) {
        this.creatorRegistry = creatorRegistry;
        this.manager = manager;
    }

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition) throws ContainerException {
            Map<String, String> properties = definition.getProperties();
            String className = properties.get("class");
            ConnectionFactory factory = instantiate(className, properties);
            String name = definition.getName();
            created.add(name);
            return manager.register(name, factory, definition.getProperties());
    }

    public void release(ConnectionFactoryDefinition definition) throws ContainerException {
            String name = definition.getName();
            if (created.remove(name)) {
                ConnectionFactory factory = manager.unregister(name);
                if (factory == null) {
                    throw new ContainerException("Connection factory not found: " + name);
                }
                creatorRegistry.release(factory);
            }
    }

    private ConnectionFactory instantiate(String className, Map<String, String> props) throws ContainerException {
        try {
            ConnectionFactory factory = (ConnectionFactory) Class.forName(className).newInstance();
            for (PropertyDescriptor pd : Introspector.getBeanInfo(factory.getClass()).getPropertyDescriptors()) {
                String propName = pd.getName();
                String propValue = props.get(propName);
                Method writeMethod = pd.getWriteMethod();
                if (propValue != null && writeMethod != null) {
                    writeMethod.invoke(factory, propValue);
                }
            }
            return factory;
        } catch (InstantiationException | InvocationTargetException | IntrospectionException | ClassNotFoundException | IllegalAccessException e) {
            throw new ContainerException("Unable to create connection factory: " + className, e);
        }

    }

}

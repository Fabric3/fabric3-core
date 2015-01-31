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
 */
package org.fabric3.binding.jms.runtime.jndi;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.host.ContainerException;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryResolver;
import org.fabric3.binding.jms.spi.runtime.provider.DestinationResolver;
import org.fabric3.jndi.spi.JndiContextManager;
import org.oasisopen.sca.annotation.Reference;

/**
 * Resolves administered objects against JNDI contexts managed by the runtime {@link JndiContextManager}.
 */
public class JndiAdministeredObjectResolver implements ConnectionFactoryResolver, DestinationResolver {
    private JndiContextManager contextManager;
    private ConnectionFactoryManager factoryManager;

    public JndiAdministeredObjectResolver(@Reference JndiContextManager contextManager, @Reference ConnectionFactoryManager factoryManager) {
        this.contextManager = contextManager;
        this.factoryManager = factoryManager;
    }

    public ConnectionFactory resolve(ConnectionFactoryDefinition definition) throws ContainerException {
        try {
            String name = definition.getName();
            ConnectionFactory factory = contextManager.lookup(ConnectionFactory.class, name);
            if (factory == null) {
                return null;
            }
            return factoryManager.register(name, factory, definition.getProperties());
        } catch (NamingException e) {
            throw new ContainerException(e);
        }
    }

    public javax.jms.Destination resolve(Destination definition) throws ContainerException {
        try {
            return contextManager.lookup(javax.jms.Destination.class, definition.getName());
        } catch (NamingException e) {
            throw new ContainerException(e);
        }
    }

}

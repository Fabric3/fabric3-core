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
import java.util.List;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.runtime.resolver.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.manager.FactoryRegistrationException;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryResolver;
import org.fabric3.binding.jms.spi.runtime.provider.JmsResolutionException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that attempts to resolve a connection by searching the ConnectionFactoryManager, provider resolvers, and then JNDI.
 */
public class NeverConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private ConnectionFactoryManager manager;
    private List<ConnectionFactoryResolver> resolvers;


    public NeverConnectionFactoryStrategy(@Reference ConnectionFactoryManager manager) {
        this.manager = manager;
    }

    @Reference(required = false)
    public void setResolvers(List<ConnectionFactoryResolver> resolvers) {
        this.resolvers = resolvers;
    }


    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition) throws JmsResolutionException {
        String name = definition.getName();
        try {
            ConnectionFactory factory = manager.get(name);
            if (factory != null) {
                return factory;
            }

            for (ConnectionFactoryResolver resolver : resolvers) {
                factory = resolver.resolve(definition);
                if (factory != null) {
                    break;
                }
            }
            return manager.register(name, factory, definition.getProperties());
        } catch (FactoryRegistrationException e) {
            throw new JmsResolutionException("Error resolving connection factory: " + name, e);
        }
    }

    public void release(ConnectionFactoryDefinition definition) throws JmsResolutionException {
        // no-op
    }
}

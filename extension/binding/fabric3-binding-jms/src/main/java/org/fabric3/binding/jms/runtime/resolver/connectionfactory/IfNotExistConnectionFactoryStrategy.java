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

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.jms.runtime.resolver.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryResolver;
import org.oasisopen.sca.annotation.Reference;

/**
 * Implementation that attempts to resolve a connection by searching the {@link }ConnectionFactoryManager}, {@link ConnectionFactoryResolver}, JNDI and then, if
 * not found, creating it.
 */
@Key("IF_NOT_EXIST")
public class IfNotExistConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    private AlwaysConnectionFactoryStrategy always;
    private ConnectionFactoryManager manager;
    private List<ConnectionFactoryResolver> resolvers;

    public IfNotExistConnectionFactoryStrategy(@Reference ConnectionFactoryCreatorRegistry creatorRegistry, @Reference ConnectionFactoryManager manager) {
        this.always = new AlwaysConnectionFactoryStrategy(creatorRegistry, manager);
        this.manager = manager;
    }

    @Reference(required = false)
    public void setResolvers(List<ConnectionFactoryResolver> resolvers) {
        this.resolvers = resolvers;
    }

    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition) throws Fabric3Exception {
        String name = definition.getName();
        if (name != null) {
            // check if the connection factory has already been created
            ConnectionFactory factory = manager.get(name);
            if (factory != null) {
                return factory;
            }
            for (ConnectionFactoryResolver resolver : resolvers) {
                factory = resolver.resolve(definition);
                if (factory != null) {
                    return factory;
                }
            }
        }
        // the connection factory has not been created
        return always.getConnectionFactory(definition);

    }

    public void release(ConnectionFactoryDefinition definition) throws Fabric3Exception {
        always.release(definition);
    }
}

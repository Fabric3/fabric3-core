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
package org.fabric3.binding.jms.runtime.resolver;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.host.ContainerException;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class AdministeredObjectResolverImpl implements AdministeredObjectResolver {
    private Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies = new HashMap<>();
    private Map<CreateOption, DestinationStrategy> destinationStrategies = new HashMap<>();

    public AdministeredObjectResolverImpl(@Reference Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies,
                                          @Reference Map<CreateOption, DestinationStrategy> destinationStrategies) {
        this.factoryStrategies = factoryStrategies;
        this.destinationStrategies = destinationStrategies;
    }

    public ConnectionFactory resolve(ConnectionFactoryDefinition definition) throws ContainerException {
        CreateOption create = definition.getCreate();
        ConnectionFactoryStrategy strategy = getConnectionFactory(create);
        return strategy.getConnectionFactory(definition);
    }

    public javax.jms.Destination resolve(Destination destination, ConnectionFactory factory) throws ContainerException {
        CreateOption create = destination.getCreate();
        DestinationStrategy strategy = destinationStrategies.get(create);
        if (strategy == null) {
            throw new AssertionError("DestinationStrategy not configured: " + create);
        }
        return strategy.getDestination(destination, factory);
    }

    public void release(ConnectionFactoryDefinition definition) throws ContainerException {
        CreateOption create = definition.getCreate();
        ConnectionFactoryStrategy strategy = getConnectionFactory(create);
        strategy.release(definition);
    }

    private ConnectionFactoryStrategy getConnectionFactory(CreateOption create) {
        ConnectionFactoryStrategy strategy = factoryStrategies.get(create);
        if (strategy == null) {
            throw new AssertionError("ConnectionFactoryStrategy not configured: " + create);
        }
        return strategy;
    }

}

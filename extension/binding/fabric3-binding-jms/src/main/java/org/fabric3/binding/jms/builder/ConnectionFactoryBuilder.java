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
package org.fabric3.binding.jms.builder;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.provision.PhysicalConnectionFactory;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("org.fabric3.binding.jms.spi.provision.PhysicalConnectionFactory")
public class ConnectionFactoryBuilder implements ResourceBuilder<PhysicalConnectionFactory> {
    private ConnectionFactoryCreatorRegistry registry;
    private ConnectionFactoryManager manager;

    public ConnectionFactoryBuilder(@Reference ConnectionFactoryCreatorRegistry registry, @Reference ConnectionFactoryManager manager) {
        this.registry = registry;
        this.manager = manager;
    }

    public void build(PhysicalConnectionFactory physicalFactory) {
        ConnectionFactoryConfiguration configuration = physicalFactory.getConfiguration();
        ConnectionFactory factory = registry.create(configuration);
        String name = configuration.getName();
        Map<String, String> factoryProperties = getProperties(configuration);
        manager.register(name, factory, factoryProperties);
    }

    public void remove(PhysicalConnectionFactory definition) {
        ConnectionFactory factory = manager.unregister(definition.getConfiguration().getName());
        registry.release(factory);
    }

    private Map<String, String> getProperties(ConnectionFactoryConfiguration configuration) {
        Properties properties = configuration.getFactoryProperties();
        Map<String, String> factoryProperties = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            factoryProperties.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return factoryProperties;
    }
}
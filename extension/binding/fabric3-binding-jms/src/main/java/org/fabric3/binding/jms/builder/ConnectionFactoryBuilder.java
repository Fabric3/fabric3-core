/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.binding.jms.builder;

import javax.jms.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.provision.PhysicalConnectionFactoryResource;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class ConnectionFactoryBuilder implements ResourceBuilder<PhysicalConnectionFactoryResource> {
    private ConnectionFactoryCreatorRegistry registry;
    private ConnectionFactoryManager manager;

    public ConnectionFactoryBuilder(@Reference ConnectionFactoryCreatorRegistry registry, @Reference ConnectionFactoryManager manager) {
        this.registry = registry;
        this.manager = manager;
    }

    public void build(PhysicalConnectionFactoryResource definition) throws ContainerException {
        ConnectionFactoryConfiguration configuration = definition.getConfiguration();
        ConnectionFactory factory = registry.create(configuration);
        String name = configuration.getName();
        Map<String, String> factoryProperties = getProperties(configuration);
        manager.register(name, factory, factoryProperties);
    }

    public void remove(PhysicalConnectionFactoryResource definition) throws ContainerException {
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
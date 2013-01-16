package org.fabric3.binding.jms.runtime.connection;

import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryTemplateRegistry;

/**
 *
 */
@EagerInit
public class ConnectionFactoryTemplateRegistryImpl implements ConnectionFactoryTemplateRegistry {
    private Map<String, ConnectionFactoryConfiguration> templates = new HashMap<String, ConnectionFactoryConfiguration>();

    public void registerTemplate(ConnectionFactoryConfiguration configuration) {
        templates.put(configuration.getName(), configuration);
    }

    public ConnectionFactoryConfiguration getTemplate(String name) {
        return templates.get(name);
    }
}

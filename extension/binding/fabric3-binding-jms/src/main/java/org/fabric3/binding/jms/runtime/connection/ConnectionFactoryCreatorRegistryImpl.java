package org.fabric3.binding.jms.runtime.connection;

import javax.jms.ConnectionFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ConnectionFactoryCreatorRegistryImpl implements ConnectionFactoryCreatorRegistry {
    private Map<String, ConnectionFactoryCreator> creators = Collections.emptyMap();
    private Map<ConnectionFactory, ConnectionFactoryCreator> factories = new HashMap<>();

    @Reference(required = false)
    public void setCreators(Map<String, ConnectionFactoryCreator> creators) {
        this.creators = creators;
    }

    @SuppressWarnings({"unchecked"})
    public ConnectionFactory create(ConnectionFactoryConfiguration configuration, Map<String, String> properties) throws ConnectionFactoryCreationException {
        ConnectionFactoryCreator creator = creators.get(configuration.getProvider());
        if (creator == null) {
            throw new ConnectionFactoryCreationException("Provider not found: " + configuration.getProvider());
        }
        ConnectionFactory factory = creator.create(configuration, properties);
        factories.put(factory, creator);
        return factory;
    }

    public void release(ConnectionFactory factory) {
        ConnectionFactoryCreator creator = factories.remove(factory);
        if (creator == null) {
            return;
        }
        creator.release(factory);
    }
}

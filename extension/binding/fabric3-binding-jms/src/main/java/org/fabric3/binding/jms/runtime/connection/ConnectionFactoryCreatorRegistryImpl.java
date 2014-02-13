package org.fabric3.binding.jms.runtime.connection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreatorRegistry;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;

/**
 *
 */
public class ConnectionFactoryCreatorRegistryImpl implements ConnectionFactoryCreatorRegistry {
    private Map<Class<?>, ConnectionFactoryCreator> creators = Collections.emptyMap();
    private Map<ConnectionFactory, ConnectionFactoryCreator> factories = new HashMap<>();

    @Reference(required = false)
    public void setCreators(Map<Class<?>, ConnectionFactoryCreator> creators) {
        this.creators = creators;
    }

    @SuppressWarnings({"unchecked"})
    public ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws ConnectionFactoryCreationException {
        ConnectionFactoryCreator creator = creators.get(configuration.getClass());
        if (creator == null) {
            throw new ConnectionFactoryCreationException("Provider not found: " + configuration.getClass().getName());
        }
        ConnectionFactory factory = creator.create(configuration);
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

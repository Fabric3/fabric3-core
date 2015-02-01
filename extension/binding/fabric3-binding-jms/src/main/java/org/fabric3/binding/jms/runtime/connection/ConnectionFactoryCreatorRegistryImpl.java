package org.fabric3.binding.jms.runtime.connection;

import javax.jms.ConnectionFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.host.Fabric3Exception;
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
    public ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws Fabric3Exception {
        if (creators.isEmpty()) {
            throw new Fabric3Exception("JMS Provider not installed");
        }
        String provider = configuration.getProvider();
        ConnectionFactoryCreator creator = provider == null ? creators.values().iterator().next() : creators.get(provider);
        if (creator == null) {
            throw new Fabric3Exception("Provider not found: " + provider);
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

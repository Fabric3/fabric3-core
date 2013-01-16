package org.fabric3.binding.jms.spi.runtime.provider;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;

/**
 * Builds default connection factory configurations for a provider which can be used to instantiate connection factories. These connection factories
 * are used by the JMS binding when a connection factory is not specified in a binding configuration.
 */
public interface DefaultConnectionFactoryBuilder {

    /**
     * Creates a default XA or non-XA connection factory.
     *
     * @param name the factory name
     * @param type the connection factory type
     * @return the connection factory configuration
     */
    ConnectionFactoryConfiguration createDefaultFactory(String name, ConnectionFactoryType type);

}

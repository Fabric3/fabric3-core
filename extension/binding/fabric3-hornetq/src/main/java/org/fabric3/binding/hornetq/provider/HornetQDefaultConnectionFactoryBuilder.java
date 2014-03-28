package org.fabric3.binding.hornetq.provider;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.DefaultConnectionFactoryBuilder;

/**
 * Creates default connection factory configurations for HornetQ.
 */
@EagerInit
public class HornetQDefaultConnectionFactoryBuilder implements DefaultConnectionFactoryBuilder {

    public ConnectionFactoryConfiguration createDefaultFactory(String name, ConnectionFactoryType type) {
        ConnectionFactoryConfiguration configuration = new ConnectionFactoryConfiguration(name, "hornetmq");
        configuration.setType(type);
        return configuration;
    }
}

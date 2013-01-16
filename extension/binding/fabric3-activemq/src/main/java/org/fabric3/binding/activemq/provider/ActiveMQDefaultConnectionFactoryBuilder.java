package org.fabric3.binding.activemq.provider;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.DefaultConnectionFactoryBuilder;

/**
 * Creates default connection factory configurations for ActiveMQ.
 */
@EagerInit
public class ActiveMQDefaultConnectionFactoryBuilder implements DefaultConnectionFactoryBuilder {
    private URI defaultBrokerName;

    public ActiveMQDefaultConnectionFactoryBuilder(@Reference BrokerHelper helper) {
        this.defaultBrokerName = URI.create(helper.getDefaultBrokerName());
    }


    public ConnectionFactoryConfiguration createDefaultFactory(String name, ConnectionFactoryType type) {
        ActiveMQConnectionFactoryConfiguration configuration = new ActiveMQConnectionFactoryConfiguration(name);
        configuration.setBrokerUri(defaultBrokerName);
        configuration.setType(type);
        return configuration;
    }
}

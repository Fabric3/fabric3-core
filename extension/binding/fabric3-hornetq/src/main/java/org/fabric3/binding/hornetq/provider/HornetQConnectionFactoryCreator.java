package org.fabric3.binding.hornetq.provider;

import javax.jms.ConnectionFactory;
import java.util.Map;

import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Creates HornetQ connection factories.
 */
@EagerInit
public class HornetQConnectionFactoryCreator implements ConnectionFactoryCreator {

    public ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws ConnectionFactoryCreationException {
        Map<String, Object> parameters = configuration.getAttributes();
        TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), parameters);
        JMSFactoryType type = convertType(configuration.getType());
        return (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithHA(type, transportConfiguration);
    }

    public void release(ConnectionFactory factory) {
        if (!(factory instanceof HornetQConnectionFactory)) {
            throw new IllegalArgumentException("ConnectionFactory not of type: " + HornetQConnectionFactory.class.getName());
        }
        HornetQConnectionFactory hqFactory = (HornetQConnectionFactory) factory;
        hqFactory.close();
    }

    private JMSFactoryType convertType(ConnectionFactoryType type) {
        return ConnectionFactoryType.LOCAL == type ? JMSFactoryType.CF : JMSFactoryType.XA_CF;
    }

}

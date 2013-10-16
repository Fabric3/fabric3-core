package org.fabric3.binding.activemq.broker;

import org.fabric3.api.host.Fabric3Exception;

/**
 *
 */
public class InvalidBrokerConfigurationException extends Fabric3Exception{
    private static final long serialVersionUID = -666314924409528499L;

    public InvalidBrokerConfigurationException(String message) {
        super(message);
    }

    public InvalidBrokerConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBrokerConfigurationException(Throwable cause) {
        super(cause);
    }
}

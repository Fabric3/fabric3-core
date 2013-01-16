package org.fabric3.binding.activemq.provider;

/**
 * Helper for creating embedded brokers
 */
public interface BrokerHelper {

    /**
     * Returns the default broker name.
     *
     * @return the default broker name
     */
    String getDefaultBrokerName();
}

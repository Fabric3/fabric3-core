package org.fabric3.spi.container.builder.component;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Returns a direct connection to a channel.
 *
 * Direct connections use the underlying channel dispatch API, for example, a ring buffer for collocated channels or the underlying transport API for bindings.
 */
public interface DirectConnectionFactory {

    /**
     * Returns a direct connection to the channel.
     *
     * @param channelUri the channel URI
     * @param type       the connection type
     * @return a supplier of the connection
     */
    <T> Supplier<T> getConnection(URI channelUri, Class<T> type);

    /**
     * Returns a direct connection to the channel topic.
     *
     * @param channelUri the channel URI
     * @param type       the connection type
     * @param topic      the topic name
     * @return a supplier of the connection
     */
    <T> Supplier<T> getConnection(URI channelUri, Class<T> type, String topic);

}

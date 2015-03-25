package org.fabric3.spi.container.builder.component;

import java.net.URI;
import java.util.function.Supplier;

/**
 * Returns a direct connection to a channel.
 *
 * Direct connections use the underlying channel dispatch API, for example, a ring buffer for collocated channels or the underlying transport API for bindings.
 */
public interface DirectConnectionFactory<T> {

    /**
     * Returns a direct connection to the channel.
     *
     * @param channelUri the channel URI.
     * @return a supplier of the connection
     */
    Supplier<T> getConnection(URI channelUri);

    /**
     * Returns a direct connection to the channel topic.
     *
     * @param channelUri the channel URI.
     * @param topic      the topic name
     * @return a supplier of the connection
     */
    Supplier<T> getConnection(URI channelUri, String topic);

}

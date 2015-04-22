package org.fabric3.spi.container.builder;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

/**
 * Returns a direct connection to a channel.
 *
 * Direct connections use the underlying channel dispatch API, for example, a ring buffer for collocated channels or the underlying transport API for bindings.
 */
public interface DirectConnectionFactory {

    /**
     * Returns the connection types this factory provides.
     *
     * @return the connection types
     */
    List<Class<?>> getTypes();

    /**
     * Returns a direct connection to the channel.
     *
     * @param channelUri the channel URI
     * @param attachUri  the producer or consumer URI; together with the channel URI, these two values form a unique key
     * @param type       the connection type
     * @return a supplier of the connection
     */
    <T> Supplier<T> getConnection(URI channelUri, URI attachUri, Class<T> type);

    /**
     * Returns a direct connection to the channel topic.
     *
     * @param channelUri the channel URI
     * @param attachUri  the producer or consumer URI; together with the channel URI, these two values form a unique key
     * @param type       the connection type
     * @param topic      the topic name
     * @return a supplier of the connection
     */
    <T> Supplier<T> getConnection(URI channelUri, URI attachUri, Class<T> type, String topic);

}

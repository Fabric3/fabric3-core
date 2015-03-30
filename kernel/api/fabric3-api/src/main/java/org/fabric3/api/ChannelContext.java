package org.fabric3.api;

/**
 * Interface for interacting with channels.
 *
 * This API is intended for applications that require dynamic channel interaction, for example, when topics are not known until runtime or subscriptions must be
 * activated or deactivated during operation. Applications may also use this API to gain access to the underlying channel implementation API to perform finer
 * grained pub/sub and streaming operations.
 */
public interface ChannelContext {

    /**
     * Returns a reactive streams publisher for the channel's default topic.
     *
     * @param type the publisher event type
     * @return the publisher
     */
    // <T> Publisher<T> getPublisher(Class<T> type);

    /**
     * Returns a reactive streams publisher.
     *
     * @param type  the publisher event type
     * @param topic the topic identifier
     * @return the publisher
     */
    // <T> Publisher<T> getPublisher(Class<T> type, String topic);

    /**
     * Returns a producer implementing the given interface type for the channel's default topic.
     *
     * @param type the producer type
     * @return a producer
     */
    <T> T getProducer(Class<T> type);

    /**
     * Returns a producer implementing the given interface type.
     *
     * @param type  the producer type
     * @param topic the topic identifier
     * @return a producer
     */
    <T> T getProducer(Class<T> type, String topic);

    /**
     * Returns a consumer implementing the given interface type for the channel's default topic.
     *
     * @param type the producer type
     * @return a producer
     */
    <T> T getConsumer(Class<T> type);

    /**
     * Returns a consumer implementing the given interface type.
     *
     * @param type  the producer type
     * @param topic the topic identifier
     * @return a producer
     */
    <T> T getConsumer(Class<T> type, String topic);

    /**
     * Callback to dispose channel proxy resources.
     *
     * @param object the proxy
     */
    void close(Object object);
}

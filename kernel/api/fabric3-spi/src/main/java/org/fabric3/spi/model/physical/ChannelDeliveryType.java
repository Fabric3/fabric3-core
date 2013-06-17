package org.fabric3.spi.model.physical;

/**
 * Defines the delivery semantics for a channel.
 */
public enum ChannelDeliveryType {

    /**
     * Events are sent through a channel using default semantics
     */
    DEFAULT,

    /**
     * Events are sent through a channel and processed by consumers synchronously
     */
    SYNCHRONOUS,

    /**
     * Events are sent through a channel and processed by consumers asynchronously
     */
    ASYNCHRONOUS,

    /**
     * Events are sent asynchronously through channel and processed on a designated thread for each consumer
     */
    ASYNCHRONOUS_WORKER
}

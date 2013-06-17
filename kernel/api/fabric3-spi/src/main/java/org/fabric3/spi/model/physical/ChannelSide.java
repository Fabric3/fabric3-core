package org.fabric3.spi.model.physical;

/**
 * The side of the logical channel a physical channel handles.
 */
public enum ChannelSide {
    /**
     * Handles receiving events from a binding and sending them to consumers.
     */
    CONSUMER,
    /**
     * Handles receiving events from producers and sending them to a binding.
     */
    PRODUCER,

    /**
     * Handles receiving events from a producer and sending them to consumers (no bindings).
     */
    COLLOCATED,
}

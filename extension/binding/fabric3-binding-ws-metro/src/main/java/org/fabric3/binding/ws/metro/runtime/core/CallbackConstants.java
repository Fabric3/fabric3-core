package org.fabric3.binding.ws.metro.runtime.core;

/**
 * Defines callback-related constants.
 */
public interface CallbackConstants {

    /**
     * Defines the callback address key when placed in a work context header.
     */
    String ENDPOINT_ADDRESS = "f3.callback.address";

    /**
     * Defines the message id key when placed in a work context header.
     */
    String MESSAGE_ID = "f3.message.id";

    /**
     * Defines the reference parameters key when placed in a work context header.
     */
    String REFERENCE_PARAMETERS = "f3.reference.parameters";
}

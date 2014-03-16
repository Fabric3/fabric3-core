package org.fabric3.spi.container.channel;

import org.fabric3.spi.container.ContainerException;

/**
 * Raised when a transformer handler cannot be created.
 */
public class HandlerCreationException extends ContainerException {
    private static final long serialVersionUID = 395183055337627858L;

    public HandlerCreationException(String message) {
        super(message);
    }

    public HandlerCreationException(Throwable cause) {
        super(cause);
    }
}

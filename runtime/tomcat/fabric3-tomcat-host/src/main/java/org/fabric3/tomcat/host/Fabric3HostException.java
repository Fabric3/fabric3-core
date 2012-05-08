package org.fabric3.tomcat.host;

import org.fabric3.host.Fabric3RuntimeException;

@SuppressWarnings("serial")
public class Fabric3HostException extends Fabric3RuntimeException {

    /**
     * Initializes the cause.
     *
     * @param cause Root cause of the exception.
     */
    public Fabric3HostException(Throwable cause) {
        super(cause);
    }

    /**
     * Initializes the message.
     *
     * @param message Message of the exception.
     */
    public Fabric3HostException(String message) {
        super(message);
    }

}

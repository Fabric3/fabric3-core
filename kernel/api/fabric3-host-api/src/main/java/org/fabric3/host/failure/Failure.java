package org.fabric3.host.failure;

/**
 * The base type for all failures raised in the system. Failures are raised during contribution introspection in response to a syntactic error or
 * during deployment when an logical instantiation or wiring exception is encountered.
 */
public abstract class Failure {

    /**
     * Returns the message associated with the error.
     *
     * @return the message associated with the error.
     */
    public abstract String getMessage();

    /**
     * Returns the abbreviated  message associated with the error.
     *
     * @return the abbreviated message associated with the error.
     */
    public String getShortMessage() {
        return getMessage();
    }

}

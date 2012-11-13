package org.fabric3.cache.introspection;

import org.fabric3.host.failure.ValidationFailure;

/**
 *
 */
public class InvalidCacheSetter extends ValidationFailure {
    private String message;

    public InvalidCacheSetter(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

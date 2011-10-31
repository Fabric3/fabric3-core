package org.fabric3.cache.introspection;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Rev$ $Date$
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

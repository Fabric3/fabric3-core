package org.fabric3.api.annotation.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a base endpoint URI.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface EndpointUri {

    /**
     * Specifies the base binding URI.
     *
     * @return the base binding URI
     */
    String value();
}

package org.fabric3.api.annotation.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Denotes injection of the environment value on an {@link Provides} method
 */
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface Environment {
}

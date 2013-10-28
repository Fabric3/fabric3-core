package org.fabric3.api.annotation.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies a binding type annotation.
 */
@Target({ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface Binding {

    /**
     * Returns the binding type qualified name.
     *
     * @return the binding type qualified name
     */
    String value() default "";

}

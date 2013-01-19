package org.fabric3.api.annotation.wire;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation that can be applied to a component to specify its wire key. When a client is wired to a an annotated component using a <code>Map</code>
 * reference, the value of the annotation will be used as the map key.
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Key {

    String value() default "";

}

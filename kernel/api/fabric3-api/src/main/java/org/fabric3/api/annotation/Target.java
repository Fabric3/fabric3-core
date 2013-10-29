package org.fabric3.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies a single reference target service.
 */
@java.lang.annotation.Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Target {

    /**
     * Specifies the reference target.
     *
     * @return the reference target
     */
    public abstract String value() default "";
}

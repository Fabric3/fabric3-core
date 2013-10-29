package org.fabric3.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies a multiplicity reference target service.
 */
@java.lang.annotation.Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Targets {

    /**
     * Specifies the reference targets.
     *
     * @return the reference targets
     */
    public abstract String[] value() default {};
}

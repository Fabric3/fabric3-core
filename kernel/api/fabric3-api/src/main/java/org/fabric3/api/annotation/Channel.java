package org.fabric3.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation used to indicate a channel context must be provided to a component by the runtime.
 */
@java.lang.annotation.Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Channel {
    /**
     * The channel name.
     *
     * @return the channel name
     */
    public abstract String value() default "";

}

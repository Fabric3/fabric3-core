package org.fabric3.binding.ws.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.annotation.model.Binding;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 */
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Binding("{http://docs.oasis-open.org/ns/opencsa/sca/200912}binding.ws")
public @interface WebServiceBinding {

    /**
     * Specifies the service interface to bind.
     *
     * @return the service interface to bind
     */
    public Class<?> service() default Void.class;

}

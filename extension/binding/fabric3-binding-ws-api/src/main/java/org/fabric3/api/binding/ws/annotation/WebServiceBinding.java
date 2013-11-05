package org.fabric3.api.binding.ws.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.annotation.model.Binding;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configures a reference or service with the WS binding.
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

    public String uri() default "";

    /**
     * Specifies the binding name.
     *
     * @return the binding name
     */
    public String name() default "";

    public int retries() default 0;

    public String wsdlElement() default "";

    public String wsdlLocation() default "";

    public BindingConfiguration[] configuration() default {};

}

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

    /**
     * Specifies the relative service URI or absolute target URI for references.
     *
     * @return the URI
     */
    public String uri() default "";

    /**
     * Specifies the binding name.
     *
     * @return the binding name
     */
    public String name() default "";

    /**
     * Specifies the number of times to retry a web service invocation if there is a network failure.
     *
     * @return the number of times to retry
     */
    public int retries() default 0;

    /**
     * Specifies the WSDL element that defines the service contract.
     *
     * @return the WSDL element
     */
    public String wsdlElement() default "";

    /**
     * Specifies the WSDL location for the service.
     *
     * @return the WSDL location
     */
    public String wsdlLocation() default "";

    /**
     * Specifies binding configuration parameters.
     *
     * @return the binding configuration parameters
     */
    public BindingConfiguration[] configuration() default {};

}

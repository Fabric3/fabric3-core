/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.api.binding.jms.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.annotation.model.Binding;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Configures a reference or service with the JMS binding.
 */
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Binding("{http://docs.oasis-open.org/ns/opencsa/sca/200912}binding.jms")
@Repeatable(JMSAnnotations.class)
public @interface JMS {
    /**
     * Specifies the service interface to bind.
     *
     * @return the service interface to bind
     */
    Class<?> service() default Void.class;

    /**
     * Specifies the forward binding configuration.
     *
     * @return the forward binding configuration
     */
    JMSConfiguration value();

    /**
     * Specifies the callback binding configuration for bidirectional services.
     *
     * @return the callback binding configuration
     */
    JMSConfiguration callback() default @JMSConfiguration(destination = "");

    /**
     * Specifies the binding name.
     *
     * @return the binding name
     */
    String name() default "";

    /**
     * Specifies the runtime environments this annotation is activated in. If blank, the annotation is active in all environments.
     *
     * @return the environments this annotation is activated in
     */
    String[] environments() default {};

}


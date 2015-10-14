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
package org.fabric3.api.binding.zeromq.annotation;

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
 * Binds a reference or service to ZeroMQ.
 */
@Target({TYPE, FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@Binding("{urn:fabric3.org}binding.zeromq")
@Repeatable(ZeroMQBindings.class)
public @interface ZeroMQ {

    /**
     * Specifies the service interface to bind.
     *
     * @return the service interface to bind
     */
    Class<?> service() default Void.class;

    /**
     * Optionally specifies the URI of the target service if it is provided by a component hosted in the current doman.
     *
     * @return the target URI
     */
    String target() default "";

    /**
     * Optionally specifies a space-delimited list of [host:port] pairs for target services.
     *
     * @return a space-delimited list of [host:port] pairs
     */
    String addresses() default "";

    /**
     * Optionally specifies a port for the service endpoint.
     *
     * @return the port
     */
    int port() default -1;

    /**
     * Specifies the socket timeout in microseconds.
     *
     * @return the socket timeout in microseconds
     */
    long timeout() default 10;

    /**
     * Specifies the high water mark.
     *
     * @return the high water mark
     */
    long highWater() default -1;

    /**
     * Specifies the multicast rate.
     *
     * @return the multicast rate
     */
    long multicastRate() default -1;

    /**
     * Specifies the multicast recovery rate.
     *
     * @return the multicast recovery rate
     */
    long multicastRecovery() default -1;

    /**
     * Specifies the send buffer size.
     *
     * @return the send buffer size
     */
    long sendBuffer() default -1;

    /**
     * Specifies the receive buffer size.
     *
     * @return the receive buffer size
     */
    long receiveBuffer() default -1;

    /**
     * Specifies the wire format for message content.
     *
     * @return the wire format for message content
     */
    String wireFormat() default "";

    /**
     * Specifies the runtime environments this annotation is activated in. If blank, the annotation is active in all environments.
     *
     * @return the environments this annotation is activated in
     */
    String[] environments() default {};

}

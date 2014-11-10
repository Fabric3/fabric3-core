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
public @interface ZeroMQ {

    /**
     * Specifies the service interface to bind.
     *
     * @return the service interface to bind
     */
    public Class<?> service() default Void.class;

    /**
     * Optionally specifies the URI of the target service if it is provided by a component hosted in the current doman.
     *
     * @return the target URI
     */
    public String target() default "";

    /**
     * Optionally specifies a space-delimited list of [host:port] pairs for target services.
     *
     * @return a space-delimited list of [host:port] pairs
     */
    public String addresses() default "";

    /**
     * Optionally specifies a port for the service endpoint.
     *
     * @return the port
     */
    public int port() default -1;

    /**
     * Specifies the socket timeout in microseconds.
     *
     * @return the socket timeout in microseconds
     */
    public long timeout() default 10;

    /**
     * Specifies the high water mark.
     *
     * @return the high water mark
     */
    public long highWater() default -1;

    /**
     * Specifies the multicast rate.
     *
     * @return the multicast rate
     */
    public long multicastRate() default -1;

    /**
     * Specifies the multicast recovery rate.
     *
     * @return the multicast recovery rate
     */
    public long multicastRecovery() default -1;

    /**
     * Specifies the send buffer size.
     *
     * @return the send buffer size
     */
    public long sendBuffer() default -1;

    /**
     * Specifies the receive buffer size.
     *
     * @return the receive buffer size
     */
    public long receiveBuffer() default -1;

    /**
     * Specifies the wire format for message content.
     *
     * @return the wire format for message content
     */
    public String wireFormat() default "";

}

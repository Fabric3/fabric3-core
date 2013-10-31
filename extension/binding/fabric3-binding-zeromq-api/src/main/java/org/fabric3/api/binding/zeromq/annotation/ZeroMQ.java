/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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

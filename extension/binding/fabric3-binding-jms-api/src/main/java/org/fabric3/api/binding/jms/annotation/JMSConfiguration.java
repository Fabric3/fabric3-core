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
package org.fabric3.api.binding.jms.annotation;

/**
 *
 */

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.CreateOption;
import org.fabric3.api.binding.jms.model.DestinationType;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies JMS configuration for a forward or callback service.
 */
@Target({ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface JMSConfiguration {

    /**
     * Specifies the destination name.
     *
     * @return he destination name
     */
    public String destination();

    /**
     * Specifies the destination type.
     *
     * @return the destination type
     */
    public DestinationType type() default DestinationType.QUEUE;

    /**
     * Specifies the connection factory name.
     *
     * @return the connection factory name
     */
    public String connectionFactory() default "";

    /**
     * Specifies the destination creation strategy.
     *
     * @return the destination creation strategy
     */
    public CreateOption create() default CreateOption.IF_NOT_EXIST;

    /**
     * Specifies the message selector.
     *
     * @return the message selector
     */
    public String selector() default "";

    /**
     * Specifies the correlation scheme.
     *
     * @return he correlation scheme
     */
    public CorrelationScheme correlation() default CorrelationScheme.MESSAGE_ID;

    /**
     * Specifies the administered object caching strategy.
     *
     * @return the administered object caching strategy
     */
    public CacheLevel cacheLevel() default CacheLevel.ADMINISTERED_OBJECTS;

    /**
     * Specifies the response destination for request-reply operations.
     *
     * @return the response destination for request-reply operations
     */
    public String responseDestination() default "";

    /**
     * Specifies the response connection factory for request-reply operations.
     *
     * @return the response connection factory for request-reply operations
     */
    public String responseConnectionFactory() default "";

    /**
     * Specifies the minimum number of JMS receivers to keep active.
     *
     * @return the minimum number of JMS receivers to keep active
     */
    public int minReceivers() default 1;

    /**
     * Specifies the maximum number of JMS receivers to keep active.
     *
     * @return the maximum number of JMS receivers to keep active
     */
    public int maxReceivers() default 1;

    /**
     * Specifies the idle time limit.
     *
     * @return the idle time limit
     */
    public int idleLimit() default 1;

    /**
     * Specifies the receive timeout in milliseconds.
     *
     * @return the receive timeout in milliseconds
     */
    public int receiveTimeout() default 15000;

    /**
     * Specifies the response timeout in milliseconds.
     *
     * @return the response timeout in milliseconds
     */
    public int responseTimeout() default 600000;

    /**
     * Specifies the maximum number of messages to process by a JMS receiver during its execution window.
     *
     * @return the maximum number of messages to process
     */
    public int maxMessagesToProcess() default -1;

    /**
     * Specifies the recovery interval.
     *
     * @return he recovery interval
     */
    public long recoveryInterval() default 5000;

    /**
     * Specifies if messages are durable.
     *
     * @return if messages are durable
     */
    public boolean durable() default false;

    /**
     * Specifies if local delivery should be done.
     *
     * @return local delivery
     */
    public boolean localDelivery() default false;

    /**
     * Specifies the client identifier for subscriptions.
     *
     * @return the client identifier
     */
    public String clientIdSpecifier() default "";

}

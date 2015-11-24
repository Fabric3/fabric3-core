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
    String destination();

    /**
     * Specifies the destination type.
     *
     * @return the destination type
     */
    DestinationType type() default DestinationType.QUEUE;

    /**
     * Specifies the connection factory name.
     *
     * @return the connection factory name
     */
    String connectionFactory() default "";

    /**
     * Specifies the destination creation strategy.
     *
     * @return the destination creation strategy
     */
    CreateOption create() default CreateOption.IF_NOT_EXIST;

    /**
     * Specifies the message selector.
     *
     * @return the message selector
     */
    String selector() default "";

    /**
     * Specifies the correlation scheme.
     *
     * @return he correlation scheme
     */
    CorrelationScheme correlation() default CorrelationScheme.MESSAGE_ID;

    /**
     * Specifies the administered object caching strategy.
     *
     * @return the administered object caching strategy
     */
    CacheLevel cacheLevel() default CacheLevel.ADMINISTERED_OBJECTS;

    /**
     * Specifies the response destination for request-reply operations.
     *
     * @return the response destination for request-reply operations
     */
    String responseDestination() default "";

    /**
     * Specifies the response connection factory for request-reply operations.
     *
     * @return the response connection factory for request-reply operations
     */
    String responseConnectionFactory() default "";

    /**
     * Specifies the minimum number of JMS receivers to keep active.
     *
     * @return the minimum number of JMS receivers to keep active
     */
    int minReceivers() default 1;

    /**
     * Specifies the maximum number of JMS receivers to keep active.
     *
     * @return the maximum number of JMS receivers to keep active
     */
    int maxReceivers() default 1;

    /**
     * Specifies the idle time limit.
     *
     * @return the idle time limit
     */
    int idleLimit() default 1;

    /**
     * Specifies the receive timeout in milliseconds.
     *
     * @return the receive timeout in milliseconds
     */
    int receiveTimeout() default 15000;

    /**
     * Specifies the response timeout in milliseconds.
     *
     * @return the response timeout in milliseconds
     */
    int responseTimeout() default 600000;

    /**
     * Specifies the maximum number of messages to process by a JMS receiver during its execution window.
     *
     * @return the maximum number of messages to process
     */
    int maxMessagesToProcess() default -1;

    /**
     * Specifies the recovery interval.
     *
     * @return he recovery interval
     */
    long recoveryInterval() default 5000;

    /**
     * Specifies if messages are durable.
     *
     * @return if messages are durable
     */
    boolean durable() default false;

    /**
     * Specifies if local delivery should be done.
     *
     * @return local delivery
     */
    boolean localDelivery() default false;

    /**
     * Specifies the subscription id for durable messaging.
     *
     * @return the subscription id
     */
    String subscriptionId() default "";

    /**
     * Specifies if the connection should use client acknowledgement.
     *
     * @return if the connection should use client acknowledgement
     */
    boolean clientAcknowledge() default false;

    /**
     * Specifies the backoff period to wait for re-attempting to listen on a destination if a set number of errors are received.
     *
     * @return the period in milliseconds
     */
    long backoffPeriod() default -1;

}

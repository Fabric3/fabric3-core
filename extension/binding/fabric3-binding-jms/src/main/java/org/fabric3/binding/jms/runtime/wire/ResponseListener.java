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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime.wire;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.api.binding.jms.model.CorrelationScheme;

/**
 * MessageListener that blocks for responses from a service provider. This listener is attached to the reference side of a wire.
 */
public class ResponseListener {
    private Destination destination;
    private CorrelationScheme scheme;

    /**
     * Constructor.
     *
     * @param destination the response destination
     * @param scheme      the correlation scheme
     */
    public ResponseListener(Destination destination, CorrelationScheme scheme) {
        this.destination = destination;
        this.scheme = scheme;
    }

    /**
     * Returns the destination for the listener
     *
     * @return the destination
     */
    public Destination getDestination() {
        return destination;
    }

    /**
     * Performs a blocking receive, i.e. control will not be returned to application code until a response is received.
     *
     * @param correlationId Correlation id
     * @param session       the session to use for processing
     * @param timeout       the receive timeout
     * @return the received message or null if the operation timed out.
     */
    public Message receive(String correlationId, Session session, long timeout) {
        try {
            MessageConsumer consumer;
            if (CorrelationScheme.MESSAGE_ID == scheme || CorrelationScheme.CORRELATION_ID == scheme) {
                String selector = "JMSCorrelationID = '" + correlationId + "'";
                consumer = session.createConsumer(destination, selector);
            } else {
                consumer = session.createConsumer(destination);
            }
            return consumer.receive(timeout);
        } catch (JMSException e) {
            // bubble exception to the client
            throw new ServiceRuntimeException("Unable to receive response for message with correlation id: " + correlationId, e);
        }

    }

}

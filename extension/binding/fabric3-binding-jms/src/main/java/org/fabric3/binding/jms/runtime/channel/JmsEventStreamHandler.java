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
package org.fabric3.binding.jms.runtime.channel;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import java.io.Serializable;

import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * A {@link EventStreamHandler} that dispatches an event to a JMS destination.
 */
public class JmsEventStreamHandler implements EventStreamHandler {
    private Destination destination;
    private ConnectionFactory connectionFactory;
    private boolean persistent;

    public JmsEventStreamHandler(Destination destination, ConnectionFactory connectionFactory, boolean persistent) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.persistent = persistent;
    }

    public void handle(Object event, boolean endOfBatch) {
        if (!(event instanceof Serializable)) {
            throw new ServiceRuntimeException("Event type must be serializable: " + event.getClass().getName());
        }
        Serializable payload = (Serializable) event;
        Connection connection = null;
        Session session = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set the context classloader to the one that loaded the connection factory implementation.
            // this is required by some JMS providers
            Thread.currentThread().setContextClassLoader(connectionFactory.getClass().getClassLoader());
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(destination);
            if (!persistent) {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }
            Message jmsMessage = session.createObjectMessage(payload);
            // enqueue the message
            producer.send(jmsMessage);
        } catch (JMSException ex) {
            throw new ServiceRuntimeException("Unable to receive response", ex);
        } finally {
            JmsHelper.closeQuietly(session);
            JmsHelper.closeQuietly(connection);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }

}
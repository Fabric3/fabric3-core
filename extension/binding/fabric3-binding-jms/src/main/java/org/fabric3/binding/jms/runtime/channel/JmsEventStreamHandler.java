/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.channel;

import java.io.Serializable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.jms.runtime.common.JmsHelper;
import org.fabric3.spi.channel.EventStreamHandler;

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

    public void handle(Object event) {
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
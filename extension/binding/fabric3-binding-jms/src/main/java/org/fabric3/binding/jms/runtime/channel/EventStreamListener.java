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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.fabric3.binding.jms.runtime.common.ListenerMonitor;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Listens for requests sent to a destination and dispatches to a channel.
 */
public class EventStreamListener implements MessageListener {
    private ClassLoader cl;
    private ListenerMonitor monitor;
    private EventStreamHandler handler;

    public EventStreamListener(ClassLoader cl, EventStreamHandler handler, ListenerMonitor monitor) {
        this.cl = cl;
        this.handler = handler;
        this.monitor = monitor;
    }

    public void onMessage(Message request) {
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL to the target service classloader
            Thread.currentThread().setContextClassLoader(cl);
            if (request instanceof ObjectMessage) {
                ObjectMessage message = (ObjectMessage) request;
                handler.handle(message.getObject(), true);
            } else if (request instanceof TextMessage) {
                TextMessage message = (TextMessage) request;
                handler.handle(message.getText(), true);
            } else {
                String type = request.getClass().getName();
                monitor.invalidMessageType(type);
            }
        } catch (JMSException e) {
            // TODO This could be a temporary error and should be sent to a dead letter queue. For now, just log the error.
            monitor.redeliveryError(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

}
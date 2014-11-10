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
package org.fabric3.binding.jms.spi.runtime.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import java.util.List;

/**
 * Proxies an XA connection factory to create a single connection that can be shared among multiple clients.
 */
public class XaSingletonConnectionFactory extends SingletonConnectionFactory implements XATopicConnectionFactory, XAQueueConnectionFactory {

    /**
     * Constructor.
     *
     * @param factory the underlying connection factory
     * @param monitor the connection monitor
     */
    public XaSingletonConnectionFactory(ConnectionFactory factory, ConnectionMonitor monitor) {
        super(factory, monitor);
        if (!(factory instanceof XAConnectionFactory)) {
            throw new IllegalArgumentException("Factory must implement XAConnectionFactory: " + factory.getClass().getName());
        }
    }

    public XAConnection createXAConnection() throws JMSException {
        return (XAConnection) createConnection();
    }

    public XAConnection createXAConnection(String userName, String password) throws JMSException {
        return (XAConnection) createConnection();
    }

    public XAQueueConnection createXAQueueConnection() throws JMSException {
        return ((XAQueueConnection) createConnection());
    }

    public XAQueueConnection createXAQueueConnection(String userName, String password) throws JMSException {
        throw new javax.jms.IllegalStateException(XaSingletonConnectionFactory.class.getName() + " does not support custom username and password");
    }

    public XATopicConnection createXATopicConnection() throws JMSException {
        return ((XATopicConnection) createConnection());

    }

    public XATopicConnection createXATopicConnection(String userName, String password) throws JMSException {
        throw new javax.jms.IllegalStateException(XaSingletonConnectionFactory.class.getName() + " does not support custom username and password");
    }

    protected List<Class> getConnectionInterfaces(Connection target) {
        List<Class> classes = super.getConnectionInterfaces(target);
        classes.add(XAConnection.class);
        if (target instanceof XATopicConnection) {
            classes.add(XATopicConnection.class);
        }
        if (target instanceof XAQueueConnection) {
            classes.add(XAQueueConnection.class);
        }
        return classes;
    }

    protected Connection createSingletonConnection() throws JMSException {
        return ((XAConnectionFactory) targetFactory).createXAConnection();
    }
}

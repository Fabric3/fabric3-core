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
package org.fabric3.binding.jms.runtime.container;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.net.URI;

import org.fabric3.binding.jms.runtime.common.JmsHelper;

/**
 * Maintains shared JMS connection state for an {@link AdaptiveMessageContainer}.
 */
public class ConnectionManager {
    private URI listenerUri;
    private ConnectionFactory connectionFactory;
    private boolean cacheConnection;
    private boolean durable;

    private MessageContainerMonitor monitor;

    private Connection sharedConnection;
    private boolean sharedConnectionStarted = false;

    /**
     * Constructor.
     *
     * @param connectionFactory the connection factory to use for creating JMS resources
     * @param listenerUri       the listener URI, typically a service or consumer
     * @param cacheConnection   true if the JMS connection should be cached. Generally, this is false if the underlying JMS provider supports transparent
     *                          caching.
     * @param durable           true if the connection must be configured for durable messages
     * @param monitor           the monitor for reporting events and errors
     */
    public ConnectionManager(ConnectionFactory connectionFactory, URI listenerUri, boolean cacheConnection, boolean durable, MessageContainerMonitor monitor) {
        this.listenerUri = listenerUri;
        this.connectionFactory = connectionFactory;
        this.cacheConnection = cacheConnection;
        this.durable = durable;
        this.monitor = monitor;
    }

    public boolean isDurable() {
        return durable;
    }

    public void start() throws JMSException {
        if (cacheConnection) {
            getSharedConnection();
        }
    }

    public Connection getConnection() throws JMSException {
        if (cacheConnection) {
            return getSharedConnection();
        } else {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Connection connection;
            try {
                // set TCCL since some JMS providers require it
                Thread.currentThread().setContextClassLoader(connectionFactory.getClass().getClassLoader());
                connection = connectionFactory.createConnection();
                connection.start();
                return connection;
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    /**
     * Closes the shared connection
     */
    public synchronized void close() {
        JmsHelper.closeQuietly(sharedConnection);
        sharedConnection = null;
    }

    /**
     * Returns a shared connection
     *
     * @return the shared connection
     * @throws JMSException if there was an error returning the shared connection
     */
    private synchronized Connection getSharedConnection() throws JMSException {
        if (sharedConnection == null) {
            sharedConnection = createSharedConnection();
        }
        return sharedConnection;
    }

    /**
     * Create a shared connection.
     *
     * @return the connection
     * @throws JMSException if an error is encountered creating the connection
     */
    private Connection createSharedConnection() throws JMSException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Connection connection = null;
        try {
            // set TCCL since some JMS providers require it
            Thread.currentThread().setContextClassLoader(connectionFactory.getClass().getClassLoader());
            connection = connectionFactory.createConnection();
            return connection;
        } catch (JMSException ex) {
            JmsHelper.closeQuietly(connection);
            throw ex;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Starts a shared connection.
     */
    public synchronized void startSharedConnection() {
        try {
            sharedConnectionStarted = true;
            if (sharedConnection != null) {
                sharedConnection.start();
            }
        } catch (JMSException e) {
            monitor.startConnectionError(e);
        }
    }

    /**
     * Stops a shared connection.
     */
    public synchronized void stopSharedConnection() {
        try {
            sharedConnectionStarted = false;
            if (sharedConnection != null) {
                sharedConnection.stop();
            }
        } catch (Exception e) {
            monitor.stopConnectionError(listenerUri, e);
        }
    }

    /**
     * Refreshes the shared connection.
     *
     * @return true if the connection was refreshed successfully
     */
    public synchronized boolean refreshConnection() {
        try {
            if (cacheConnection) {
                refreshSharedConnection();
            } else {
                // set TCCL since some JMS providers require it
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Connection con = null;
                try {
                    Thread.currentThread().setContextClassLoader(connectionFactory.getClass().getClassLoader());
                    con = connectionFactory.createConnection();
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                    JmsHelper.closeQuietly(con);
                }
            }
            return true;
        } catch (Exception e) {
            monitor.connectionError(listenerUri.toString(), e);
            return false;
        }
    }

    /**
     * Refreshes the shared connection.
     *
     * @throws JMSException there is an error refreshing the connection
     */
    private synchronized void refreshSharedConnection() throws JMSException {
        JmsHelper.closeQuietly(sharedConnection);
        sharedConnection = createSharedConnection();
        if (sharedConnectionStarted) {
            sharedConnection.start();
        }
    }

}

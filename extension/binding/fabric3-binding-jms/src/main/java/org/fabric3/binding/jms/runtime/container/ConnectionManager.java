/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.fabric3.binding.jms.runtime.common.JmsHelper;

/**
 * Maintains shared JMS connection state for an {@link AdaptiveMessageContainer}.
 *
 * @version $Rev$ $Date$
 */
public class ConnectionManager {
    private URI listenerUri;
    private ConnectionFactory connectionFactory;
    private String clientId;
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
     * @param clientId          the JMS client id
     * @param cacheConnection   true if the JMS connection should be cached. Generally, this is false if the underlying JMS provider supports
     *                          transparent caching.
     * @param durable           true if the connection must be configured for durable messages
     * @param monitor           the monitor for reporting events and errors
     */
    public ConnectionManager(ConnectionFactory connectionFactory,
                             URI listenerUri,
                             String clientId,
                             boolean cacheConnection,
                             boolean durable,
                             MessageContainerMonitor monitor) {
        this.listenerUri = listenerUri;
        this.connectionFactory = connectionFactory;
        this.clientId = clientId;
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
            Connection connection = connectionFactory.createConnection();
            if (durable) {
                connection.setClientID(clientId);
            }
            connection.start();
            return connection;
        }
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
     * Closes the shared connection
     */
    public synchronized void close() {
        JmsHelper.closeQuietly(sharedConnection);
        sharedConnection = null;
    }

    /**
     * Create a shared connection.
     *
     * @return the connection
     * @throws JMSException if an error is encountered creating the connection
     */
    private Connection createSharedConnection() throws JMSException {
        Connection connection = connectionFactory.createConnection();
        try {
            if (clientId != null) {
                connection.setClientID(clientId);
            }
            return connection;
        }
        catch (JMSException ex) {
            JmsHelper.closeQuietly(connection);
            throw ex;
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
    public boolean refreshConnection() {
        try {
            if (cacheConnection) {
                refreshSharedConnection();
            } else {
                Connection con = connectionFactory.createConnection();
                JmsHelper.closeQuietly(con);
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

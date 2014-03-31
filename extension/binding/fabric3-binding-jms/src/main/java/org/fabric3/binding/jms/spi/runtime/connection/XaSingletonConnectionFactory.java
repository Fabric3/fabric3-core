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

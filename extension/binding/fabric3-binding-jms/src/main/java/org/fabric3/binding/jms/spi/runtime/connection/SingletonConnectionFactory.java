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
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Proxies a connection factory to create a single connection that can be shared among multiple clients. This is class is used for durable subscriptions that
 * require a unique connection id.
 * <p/>
 * This implementation will re-initialize the singleton connection if a connection exception is reported by the JMS provider.
 */
public class SingletonConnectionFactory implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory, ExceptionListener {
    protected ConnectionFactory targetFactory;
    private ConnectionMonitor monitor;

    protected Connection targetConnection;
    protected Connection proxiedConnection;
    protected boolean started = false;

    protected final Object sync = new Object();

    /**
     * Constructor.
     *
     * @param factory the underlying connection factory
     * @param monitor the connection monitor
     */
    public SingletonConnectionFactory(ConnectionFactory factory, ConnectionMonitor monitor) {
        this.targetFactory = factory;
        this.monitor = monitor;
    }

    public Connection createConnection() throws JMSException {
        synchronized (sync) {
            if (proxiedConnection == null) {
                init();
            }
            return proxiedConnection;
        }
    }

    public Connection createConnection(String username, String password) throws JMSException {
        throw new javax.jms.IllegalStateException("Operation not supported");
    }

    public QueueConnection createQueueConnection() throws JMSException {
        return ((QueueConnection) createConnection());
    }

    public QueueConnection createQueueConnection(String username, String password) throws JMSException {
        throw new javax.jms.IllegalStateException("Operation not supported");
    }

    public TopicConnection createTopicConnection() throws JMSException {
        return ((TopicConnection) createConnection());
    }

    public TopicConnection createTopicConnection(String username, String password) throws JMSException {
        throw new javax.jms.IllegalStateException(SingletonConnectionFactory.class.getName() + " does not support username and password");
    }

    /**
     * Exception callback that resets the underlying connection.
     */
    public void onException(JMSException exception) {
        resetConnection();
    }

    /**
     * Initializes the factory, creating an shared connection.
     *
     * @throws javax.jms.JMSException if an initialization error is encountered
     */
    public void init() throws JMSException {
        synchronized (sync) {
            if (targetConnection != null) {
                closeConnection(targetConnection);
            }
            targetConnection = createSingletonConnection();
            targetConnection.setExceptionListener(this);
            proxiedConnection = proxyConnection(targetConnection);
        }
    }

    /**
     * Closes the underlying connection.
     */
    public void destroy() {
        resetConnection();
    }

    /**
     * Returns the interfaces to be implemented by the connection proxy. Subclasses may chose to implement additional interface types.
     *
     * @param target the connection to proxy
     * @return the implemented interfaces
     */
    protected List<Class> getConnectionInterfaces(Connection target) {
        List<Class> classes = new ArrayList<>();
        classes.add(Connection.class);
        if (target instanceof TopicConnection) {
            classes.add(TopicConnection.class);
        }
        if (target instanceof QueueConnection) {
            classes.add(QueueConnection.class);
        }
        return classes;
    }

    /**
     * Creates the underlying singleton connection. Subclasses may override to produce a specific connection type.
     *
     * @return the connection
     * @throws JMSException if there is an error creating the connection
     */
    protected Connection createSingletonConnection() throws JMSException {
        return targetFactory.createConnection();
    }

    /**
     * Resets the underlying connection.
     */
    private void resetConnection() {
        synchronized (sync) {
            if (targetConnection != null) {
                closeConnection(targetConnection);
            }
            targetConnection = null;
            proxiedConnection = null;
        }
    }

    /**
     * Close the given Connection.
     *
     * @param connection the Connection to close
     */
    private void closeConnection(Connection connection) {
        try {
            try {
                if (started) {
                    started = false;
                    connection.stop();
                }
            } finally {
                connection.close();
            }
        } catch (Throwable e) {
            monitor.error(e);
        }
    }

    /**
     * Proxies the given Connection and suppresses close calls.
     *
     * @param target the connection to proxy
     * @return the proxy
     */
    private Connection proxyConnection(Connection target) {
        List<Class> classes = getConnectionInterfaces(target);
        Class[] interfaces = classes.toArray(new Class[classes.size()]);
        ClassLoader classLoader = Connection.class.getClassLoader();
        ConnectionInvocationHandler handler = new ConnectionInvocationHandler(target);
        return (Connection) Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    /**
     * Proxies the underlying connection. Suppresses calls to {@link Connection#close()} and {@link Connection#stop()}.
     */
    private class ConnectionInvocationHandler implements InvocationHandler {
        private final Connection target;

        public ConnectionInvocationHandler(Connection target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.equals("equals")) {
                return (proxy == args[0]);
            } else if (name.equals("hashCode")) {
                return System.identityHashCode(proxy);
            } else if (name.equals("toString")) {
                return "Singleton connection proxy: " + target;
            } else if (name.equals("setClientID")) {
                throw new javax.jms.IllegalStateException("setClientId() not supported on proxied connections");
            } else if (name.equals("setExceptionListener")) {
                throw new javax.jms.IllegalStateException("setExceptionListener() not supported on proxied connections");
            } else if (name.equals("start")) {
                synchronized (sync) {
                    if (!started) {
                        target.start();
                        started = true;
                    }
                }
                return null;
            } else if (name.equals("stop")) {
                // suppress
                return null;
            } else if (name.equals("close")) {
                // suppress
                return null;
            }
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

}
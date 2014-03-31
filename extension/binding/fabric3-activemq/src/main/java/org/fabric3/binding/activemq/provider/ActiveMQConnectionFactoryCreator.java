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
package org.fabric3.binding.activemq.provider;

import javax.jms.ConnectionFactory;
import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionMonitor;
import org.fabric3.binding.jms.spi.runtime.connection.SingletonConnectionFactory;
import org.fabric3.binding.jms.spi.runtime.connection.XaSingletonConnectionFactory;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates ActiveMQ connection factories on demand.
 */
@EagerInit
public class ActiveMQConnectionFactoryCreator implements ConnectionFactoryCreator {
    private URI brokerUri;
    private HostInfo info;
    private ConnectionMonitor monitor;

    public ActiveMQConnectionFactoryCreator(@Reference HostInfo info, @Monitor ConnectionMonitor monitor) {
        this.info = info;
        this.monitor = monitor;
        String brokerName = info.getRuntimeName().replace(":", ".");
        brokerUri = URI.create("vm://" + brokerName);
    }

    public ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws ConnectionFactoryCreationException {
        ConnectionFactoryType type = configuration.getType();
        String clientId = configuration.getClientId();
        switch (type) {

            case XA:
                ActiveMQXAConnectionFactory xaFactory = new ActiveMQXAConnectionFactory(getUri(configuration));
                xaFactory.setProperties(configuration.getFactoryProperties());
                xaFactory.setUserName(configuration.getUsername());
                xaFactory.setPassword(configuration.getPassword());
                if (clientId != null) {
                    // since a client id is specified (possibly for a durable subscription), create a singleton connection so the connection id is unique
                    setClientId(clientId, xaFactory);
                    return new XaSingletonConnectionFactory(xaFactory, monitor);
                }
                return xaFactory;
            default:
                // default to local pooled
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(getUri(configuration));
                factory.setProperties(configuration.getFactoryProperties());
                factory.setUserName(configuration.getUsername());
                factory.setPassword(configuration.getPassword());
                if (clientId != null) {
                    // since a client id is specified (possibly for a durable subscription), create a singleton connection so the connection id is unique
                    setClientId(clientId, factory);
                    return new SingletonConnectionFactory(factory, monitor);
                }
                return factory;
        }
    }

    public void release(ConnectionFactory factory) {
        if (factory instanceof SingletonConnectionFactory) {
            // if the connection is a singleton, close the proxied connection
            ((SingletonConnectionFactory) factory).destroy();
        }
    }

    private void setClientId(String clientId, ActiveMQConnectionFactory factory) {
        if (ConnectionFactoryConfiguration.RUNTIME.equals(clientId)) {
            // client id is set to the runtime name
            factory.setClientID(info.getRuntimeName().replace(":", "."));
        } else {
            factory.setClientID(clientId);
        }
    }

    private URI getUri(ConnectionFactoryConfiguration configuration) {
        URI attribute = configuration.getAttribute(URI.class, "broker.uri");
        if (attribute != null) {
            return attribute;
        }
        return brokerUri;
    }

}

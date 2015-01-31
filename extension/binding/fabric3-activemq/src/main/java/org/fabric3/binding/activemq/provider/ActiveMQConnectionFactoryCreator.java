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
package org.fabric3.binding.activemq.provider;

import javax.jms.ConnectionFactory;
import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryConfiguration;
import org.fabric3.api.binding.jms.resource.ConnectionFactoryType;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionMonitor;
import org.fabric3.binding.jms.spi.runtime.connection.SingletonConnectionFactory;
import org.fabric3.binding.jms.spi.runtime.connection.XaSingletonConnectionFactory;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;
import org.fabric3.api.host.ContainerException;
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

    public ConnectionFactory create(ConnectionFactoryConfiguration configuration) throws ContainerException {
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

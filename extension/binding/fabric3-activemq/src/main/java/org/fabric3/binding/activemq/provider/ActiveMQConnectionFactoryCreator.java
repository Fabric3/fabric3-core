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
import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryConfiguration;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.connection.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.provider.ConnectionFactoryCreator;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates ActiveMQ connection factories on demand.
 */
@EagerInit
public class ActiveMQConnectionFactoryCreator implements ConnectionFactoryCreator {
    public static final String BROKER_URI = "broker.uri";
    private URI brokerUri;

    public ActiveMQConnectionFactoryCreator(@Reference HostInfo info) {
        String brokerName = info.getRuntimeName().replace(":", ".");
        brokerUri = URI.create("vm://" + brokerName);
    }

    public ConnectionFactory create(ConnectionFactoryConfiguration configuration, Map<String, String> properties) throws ConnectionFactoryCreationException {
        ConnectionFactoryType type = configuration.getType();
        switch (type) {

            case XA:
                ActiveMQXAConnectionFactory xaFactory = new ActiveMQXAConnectionFactory(getUri(configuration, properties));
                xaFactory.setProperties(configuration.getFactoryProperties());
                xaFactory.setUserName(configuration.getUsername());
                xaFactory.setPassword(configuration.getPassword());
                return xaFactory;
            default:
                // default to local pooled
                ActiveMQConnectionFactory wrapped = new ActiveMQConnectionFactory(getUri(configuration, properties));
                wrapped.setProperties(configuration.getFactoryProperties());
                wrapped.setUserName(configuration.getUsername());
                wrapped.setPassword(configuration.getPassword());
                return new PooledConnectionFactory(wrapped);
        }
    }

    public void release(ConnectionFactory factory) {
        if (factory instanceof PooledConnectionFactory) {
            PooledConnectionFactory pooled = (PooledConnectionFactory) factory;
            pooled.stop();
        }

    }

    private URI getUri(ConnectionFactoryConfiguration configuration, Map<String, String> properties) {
        // check if the broker uri was overridden by the JMS properties, e.g. when a connection factory template does not contain a URI but the specific
        // binding configuration on a service, reference, or channel does
        String brokerOverride = properties.get(BROKER_URI);
        if (brokerOverride != null) {
            return URI.create(brokerOverride);
        }
        URI attribute = configuration.getAttribute(URI.class, "broker.uri");
        if (attribute != null) {
            return attribute;
        }
        return brokerUri;
    }

}

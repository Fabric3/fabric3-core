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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime;

import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.runtime.host.JmsHost;
import org.fabric3.binding.jms.runtime.host.ListenerConfiguration;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.common.CacheLevel;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSourceDefinition;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

import static org.fabric3.binding.jms.spi.common.CacheLevel.ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.spi.common.CacheLevel.CONNECTION;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_CONNECTION;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_NONE;
import static org.fabric3.binding.jms.spi.runtime.JmsConstants.DEFAULT_CONNECTION_FACTORY;

/**
 * Attaches a consumer to a JMS destination.
 *
 * @version $Revision$ $Date$
 */
public class JmsConnectionSourceAttacher implements SourceConnectionAttacher<JmsConnectionSourceDefinition> {

    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private JmsHost jmsHost;
    private ListenerMonitor monitor;
    private HostInfo info;

    public JmsConnectionSourceAttacher(@Reference AdministeredObjectResolver resolver,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference JmsHost jmsHost,
                                       @Reference HostInfo info,
                                       @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.jmsHost = jmsHost;
        this.info = info;
        this.monitor = monitor;
    }

    public void attach(JmsConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        URI serviceUri = source.getUri();
        ClassLoader sourceClassLoader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ListenerConfiguration configuration = new ListenerConfiguration();
        try {
            ConnectionFactory connectionFactory = objects.getRequestFactory();
            Destination destination = objects.getRequestDestination();
            List<EventStream> streams = connection.getEventStreams();
            if (streams.size() != 1) {
                throw new ConnectionAttachException("There must be a single event stream: " + streams.size());
            }
            EventStream stream = streams.get(0);
            EventStreamListener listener = new EventStreamListener(sourceClassLoader, stream.getHeadHandler(), monitor);
            configuration.setDestination(destination);
            configuration.setFactory(connectionFactory);
            configuration.setMessageListener(listener);
            configuration.setUri(serviceUri);
            populateConfiguration(configuration, source.getMetadata());
            if (jmsHost.isRegistered(serviceUri)) {
                // the wire has changed and it is being reprovisioned
                jmsHost.unregister(serviceUri);
            }
            jmsHost.register(configuration);
        } catch (JMSException e) {
            throw new ConnectionAttachException(e);
        }
    }

    public void detach(JmsConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ConnectionAttachException {
        try {
            jmsHost.unregister(source.getUri());
        } catch (JMSException e) {
            throw new ConnectionAttachException(e);
        }
    }

    private void populateConfiguration(ListenerConfiguration configuration, JmsBindingMetadata metadata) {
        CacheLevel cacheLevel = metadata.getCacheLevel();
        if (CONNECTION == cacheLevel) {
            configuration.setCacheLevel(CACHE_CONNECTION);
        } else if (ADMINISTERED_OBJECTS == cacheLevel) {
            configuration.setCacheLevel(CACHE_ADMINISTERED_OBJECTS);
        } else {
            configuration.setCacheLevel(CACHE_NONE);
        }
        configuration.setIdleLimit(metadata.getIdleLimit());
        configuration.setMaxMessagesToProcess(metadata.getMaxMessagesToProcess());
        configuration.setMaxReceivers(metadata.getMaxReceivers());
        configuration.setMinReceivers(metadata.getMinReceivers());
        configuration.setReceiveTimeout(metadata.getReceiveTimeout());
        configuration.setTransactionTimeout(metadata.getTransactionTimeout());

        String clientId = info.getRuntimeName() + ":" + metadata.getClientIdSpecifier();
        configuration.setClientId(clientId);
        configuration.setDurable(metadata.isDurable());
//        configuration.setDeliveryMode();
//        configuration.setExceptionListener();
//        configuration.setLocalDelivery();
    }

    private ResolvedObjects resolveAdministeredObjects(JmsConnectionSourceDefinition source) throws ConnectionAttachException {
        try {
            JmsBindingMetadata metadata = source.getMetadata();
            Hashtable<String, String> env = metadata.getEnv();
            ConnectionFactoryDefinition definition = metadata.getConnectionFactory();
            ConnectionFactory requestConnectionFactory = resolver.resolve(definition, env);
            DestinationDefinition requestDestinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(requestDestinationDefinition, requestConnectionFactory, env);

            return new ResolvedObjects(requestConnectionFactory, requestDestination);
        } catch (JmsResolutionException e) {
            throw new ConnectionAttachException(e);
        }
    }

    private class ResolvedObjects {
        private ConnectionFactory requestFactory;
        private Destination requestDestination;

        private ResolvedObjects(ConnectionFactory requestFactory, Destination requestDestination) {
            this.requestFactory = requestFactory;
            this.requestDestination = requestDestination;
        }

        public ConnectionFactory getRequestFactory() {
            return requestFactory;
        }

        public Destination getRequestDestination() {
            return requestDestination;
        }

    }

}
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

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.runtime.host.JmsHost;
import org.fabric3.binding.jms.runtime.host.ListenerConfiguration;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.common.CacheLevel;
import org.fabric3.binding.jms.spi.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.spi.common.DestinationDefinition;
import org.fabric3.binding.jms.spi.common.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSourceDefinition;
import org.fabric3.binding.jms.spi.runtime.JmsConstants;
import org.fabric3.binding.jms.spi.runtime.JmsResolutionException;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Attaches a channel or consumer to a JMS destination.
 *
 * @version $Revision$ $Date$
 */
public class JmsConnectionSourceAttacher implements SourceConnectionAttacher<JmsConnectionSourceDefinition> {

    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private JmsHost jmsHost;
    private ListenerMonitor monitor;

    public JmsConnectionSourceAttacher(@Reference AdministeredObjectResolver resolver,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference JmsHost jmsHost,
                                       @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.jmsHost = jmsHost;
        this.monitor = monitor;
    }

    public void attach(JmsConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        URI serviceUri = target.getTargetUri();
        ClassLoader sourceClassLoader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ListenerConfiguration configuration = new ListenerConfiguration();
        try {
            ConnectionFactory connectionFactory = objects.getRequestFactory();
            Destination destination = objects.getRequestDestination();
            List<EventStream> streams = connection.getEventStreams();
            if (streams.size() != 1) {
                throw new ConnectionAttachException("There must be a single event stream:" + streams.size());
            }
            EventStream stream = streams.get(0);
            EventStreamListener listener = new EventStreamListener(sourceClassLoader, stream.getHeadHandler(), monitor);
            configuration.setDestination(destination);
            configuration.setFactory(connectionFactory);
            configuration.setMessageListener(listener);
            configuration.setUri(serviceUri);
            configuration.setType(TransactionType.NONE);
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

    }


    private void populateConfiguration(ListenerConfiguration configuration, JmsBindingMetadata metadata) {
        CacheLevel cacheLevel = metadata.getCacheLevel();
        if (CacheLevel.CONNECTION == cacheLevel) {
            configuration.setCacheLevel(JmsConstants.CACHE_CONNECTION);
        } else if (CacheLevel.SESSION == cacheLevel) {
            configuration.setCacheLevel(JmsConstants.CACHE_SESSION);
        } else {
            configuration.setCacheLevel(JmsConstants.CACHE_NONE);
        }
        configuration.setIdleLimit(metadata.getIdleLimit());
        configuration.setMaxMessagesToProcess(metadata.getMaxMessagesToProcess());
        configuration.setMaxReceivers(metadata.getMaxReceivers());
        configuration.setMinReceivers(metadata.getMinReceivers());
        configuration.setReceiveTimeout(metadata.getReceiveTimeout());
        configuration.setTransactionTimeout(metadata.getTransactionTimeout());
//        configuration.setDeliveryMode();
//        configuration.setDurableSubscriptionName();
//        configuration.setExceptionListener();
//        configuration.setClientId();
//        configuration.setLocalDelivery();
    }

    private ResolvedObjects resolveAdministeredObjects(JmsConnectionSourceDefinition source) throws ConnectionAttachException {
        try {
            JmsBindingMetadata metadata = source.getMetadata();
            Hashtable<String, String> env = metadata.getEnv();
            ConnectionFactoryDefinition requestConnectionFactoryDefinition = metadata.getConnectionFactory();
            requestConnectionFactoryDefinition.setName(JmsConstants.DEFAULT_CONNECTION_FACTORY);

            ConnectionFactory requestConnectionFactory = resolver.resolve(requestConnectionFactoryDefinition, env);
            DestinationDefinition requestDestinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(requestDestinationDefinition, requestConnectionFactory, env);

            ConnectionFactory responseConnectionFactory = null;
            Destination responseDestination = null;
            return new ResolvedObjects(requestConnectionFactory, requestDestination, responseConnectionFactory, responseDestination);
        } catch (JmsResolutionException e) {
            throw new ConnectionAttachException(e);
        }
    }

    private class ResolvedObjects {
        private ConnectionFactory requestFactory;
        private ConnectionFactory responseFactory;
        private Destination requestDestination;
        private Destination responseDestination;

        private ResolvedObjects(ConnectionFactory requestFactory,
                                Destination requestDestination,
                                ConnectionFactory responseFactory,
                                Destination responseDestination) {
            this.requestFactory = requestFactory;
            this.requestDestination = requestDestination;
            this.responseFactory = responseFactory;
            this.responseDestination = responseDestination;
        }

        public ConnectionFactory getRequestFactory() {
            return requestFactory;
        }

        public ConnectionFactory getResponseFactory() {
            return responseFactory;
        }

        public Destination getRequestDestination() {
            return requestDestination;
        }

        public Destination getResponseDestination() {
            return responseDestination;
        }
    }

}
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.binding.jms.runtime.channel.EventStreamListener;
import org.fabric3.binding.jms.runtime.common.ListenerMonitor;
import org.fabric3.binding.jms.runtime.container.ContainerConfiguration;
import org.fabric3.binding.jms.runtime.container.MessageContainerManager;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.binding.jms.model.CacheLevel.ADMINISTERED_OBJECTS;
import static org.fabric3.api.binding.jms.model.CacheLevel.CONNECTION;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_CONNECTION;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_NONE;

/**
 * Attaches a consumer to a JMS destination.
 */
public class JmsConnectionSourceAttacher implements SourceConnectionAttacher<JmsConnectionSourceDefinition> {

    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private MessageContainerManager containerManager;
    private ListenerMonitor monitor;

    public JmsConnectionSourceAttacher(@Reference AdministeredObjectResolver resolver,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference MessageContainerManager containerManager,
                                       @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.containerManager = containerManager;
        this.monitor = monitor;
    }

    public void attach(JmsConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        URI serviceUri = source.getUri();
        ClassLoader sourceClassLoader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        JmsBindingMetadata metadata = source.getMetadata();

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ContainerConfiguration configuration = new ContainerConfiguration();
        try {
            ConnectionFactory connectionFactory = objects.getRequestFactory();
            Destination destination = objects.getRequestDestination();
            EventStream stream = connection.getEventStream();
            EventStreamListener listener = new EventStreamListener(sourceClassLoader, stream.getHeadHandler(), monitor);
            configuration.setDestinationType(metadata.getDestination().geType());
            configuration.setDestination(destination);
            configuration.setFactory(connectionFactory);
            configuration.setMessageListener(listener);
            configuration.setUri(serviceUri);
            populateConfiguration(configuration, metadata);
            if (containerManager.isRegistered(serviceUri)) {
                // the wire has changed and it is being reprovisioned
                containerManager.unregister(serviceUri);
            }
            containerManager.register(configuration);
        } catch (JMSException e) {
            throw new ContainerException(e);
        }
    }

    public void detach(JmsConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {
        try {
            containerManager.unregister(source.getUri());
            resolver.release(source.getMetadata().getConnectionFactory());
        } catch (JMSException e) {
            throw new ContainerException(e);
        }
    }

    private void populateConfiguration(ContainerConfiguration configuration, JmsBindingMetadata metadata) {
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
        configuration.setSubscriptionId(metadata.getSubscriptionId());
        configuration.setDurable(metadata.isDurable());
        //        configuration.setDeliveryMode();
        //        configuration.setExceptionListener();
        //        configuration.setLocalDelivery();
    }

    private ResolvedObjects resolveAdministeredObjects(JmsConnectionSourceDefinition source) throws ContainerException {
        JmsBindingMetadata metadata = source.getMetadata();
        ConnectionFactoryDefinition definition = metadata.getConnectionFactory();
        ConnectionFactory requestConnectionFactory = resolver.resolve(definition);
        DestinationDefinition requestDestinationDefinition = metadata.getDestination();
        Destination requestDestination = resolver.resolve(requestDestinationDefinition, requestConnectionFactory);
        return new ResolvedObjects(requestConnectionFactory, requestDestination);
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
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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.runtime;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import java.net.URI;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.binding.jms.runtime.channel.EventStreamListener;
import org.fabric3.binding.jms.runtime.common.ListenerMonitor;
import org.fabric3.binding.jms.runtime.container.AdaptiveMessageContainer;
import org.fabric3.binding.jms.runtime.container.ContainerConfiguration;
import org.fabric3.binding.jms.runtime.container.MessageContainerFactory;
import org.fabric3.binding.jms.runtime.container.MessageContainerManager;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.spi.provision.JmsConnectionSource;
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
public class JmsConnectionSourceAttacher implements SourceConnectionAttacher<JmsConnectionSource> {

    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private MessageContainerFactory containerFactory;
    private MessageContainerManager containerManager;
    private ListenerMonitor monitor;

    public JmsConnectionSourceAttacher(@Reference AdministeredObjectResolver resolver,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference MessageContainerFactory containerFactory,
                                       @Reference MessageContainerManager containerManager,
                                       @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.containerFactory = containerFactory;
        this.containerManager = containerManager;
        this.monitor = monitor;
    }

    public void attach(JmsConnectionSource source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        URI serviceUri = source.getUri();
        ClassLoader sourceClassLoader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        JmsBindingMetadata metadata = source.getMetadata();

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ContainerConfiguration configuration = new ContainerConfiguration();
        try {
            ConnectionFactory connectionFactory = objects.getRequestFactory();
            javax.jms.Destination destination = objects.getRequestDestination();
            EventStream stream = connection.getEventStream();
            EventStreamListener listener = new EventStreamListener(sourceClassLoader, stream.getHeadHandler(), monitor);
            configuration.setDestinationType(metadata.getDestination().geType());
            configuration.setDestination(destination);
            configuration.setFactory(connectionFactory);
            configuration.setMessageListener(listener);
            configuration.setUri(serviceUri);
            configuration.setSessionType(source.getSessionType());
            populateConfiguration(configuration, metadata);
            if (containerManager.isRegistered(serviceUri)) {
                // the wire has changed and it is being reprovisioned
                containerManager.unregister(serviceUri);
            }
            AdaptiveMessageContainer container = containerFactory.create(configuration);
            containerManager.register(container);
        } catch (JMSException e) {
            throw new ContainerException(e);
        }
    }

    public void detach(JmsConnectionSource source, PhysicalConnectionTargetDefinition target) throws ContainerException {
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

    private ResolvedObjects resolveAdministeredObjects(JmsConnectionSource source) throws ContainerException {
        JmsBindingMetadata metadata = source.getMetadata();
        ConnectionFactoryDefinition connectionFactory = metadata.getConnectionFactory();
        ConnectionFactory requestConnectionFactory = resolver.resolve(connectionFactory);
        Destination requestDestinationDefinition = metadata.getDestination();
        javax.jms.Destination requestDestination = resolver.resolve(requestDestinationDefinition, requestConnectionFactory);
        return new ResolvedObjects(requestConnectionFactory, requestDestination);
    }

    private class ResolvedObjects {
        private ConnectionFactory requestFactory;
        private javax.jms.Destination requestDestination;

        private ResolvedObjects(ConnectionFactory requestFactory, javax.jms.Destination requestDestination) {
            this.requestFactory = requestFactory;
            this.requestDestination = requestDestination;
        }

        public ConnectionFactory getRequestFactory() {
            return requestFactory;
        }

        public javax.jms.Destination getRequestDestination() {
            return requestDestination;
        }

    }

}
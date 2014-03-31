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
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.jms.model.CacheLevel;
import org.fabric3.api.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.api.binding.jms.model.CorrelationScheme;
import org.fabric3.api.binding.jms.model.DestinationDefinition;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.binding.jms.runtime.common.ListenerMonitor;
import org.fabric3.binding.jms.runtime.container.AdaptiveMessageContainer;
import org.fabric3.binding.jms.runtime.container.ContainerConfiguration;
import org.fabric3.binding.jms.runtime.container.MessageContainerFactory;
import org.fabric3.binding.jms.runtime.container.MessageContainerManager;
import org.fabric3.binding.jms.runtime.resolver.AdministeredObjectResolver;
import org.fabric3.binding.jms.runtime.wire.InvocationChainHolder;
import org.fabric3.binding.jms.runtime.wire.ServiceListener;
import org.fabric3.binding.jms.runtime.wire.WireHolder;
import org.fabric3.binding.jms.spi.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.spi.provision.OperationPayloadTypes;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.api.binding.jms.model.CacheLevel.ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_ADMINISTERED_OBJECTS;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_CONNECTION;
import static org.fabric3.binding.jms.runtime.common.JmsRuntimeConstants.CACHE_NONE;

/**
 * Attaches a channel or consumer to a JMS destination.
 */
public class JmsSourceWireAttacher implements SourceWireAttacher<JmsWireSourceDefinition> {

    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private MessageContainerFactory containerFactory;
    private MessageContainerManager containerManager;
    private ListenerMonitor monitor;
    private XMLFactory xmlFactory;
    private BindingHandlerRegistry handlerRegistry;

    public JmsSourceWireAttacher(@Reference AdministeredObjectResolver resolver,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference MessageContainerFactory containerFactory,
                                 @Reference MessageContainerManager containerManager,
                                 @Reference XMLFactory xmlFactory,
                                 @Reference BindingHandlerRegistry handlerRegistry,
                                 @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.containerFactory = containerFactory;
        this.containerManager = containerManager;
        this.xmlFactory = xmlFactory;
        this.monitor = monitor;
        this.handlerRegistry = handlerRegistry;
    }

    public void attach(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws ContainerException {
        URI serviceUri = target.getUri();
        ClassLoader loader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        SessionType trxType = source.getSessionType();
        WireHolder wireHolder = createWireHolder(wire, source, trxType);

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ContainerConfiguration configuration = new ContainerConfiguration();
        try {
            ConnectionFactory requestFactory = objects.getRequestFactory();
            Destination requestDestination = objects.getRequestDestination();
            ConnectionFactory responseFactory = objects.getResponseFactory();
            Destination responseDestination = objects.getResponseDestination();

            List<BindingHandler<Message>> handlers = createHandlers(source);
            ServiceListener listener = new ServiceListener(wireHolder, responseDestination, responseFactory, trxType, loader, xmlFactory, handlers, monitor);

            configuration.setDestination(requestDestination);
            configuration.setFactory(requestFactory);
            configuration.setMessageListener(listener);
            configuration.setUri(serviceUri);
            configuration.setType(trxType);
            populateConfiguration(configuration, source.getMetadata());

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

    private void populateConfiguration(ContainerConfiguration configuration, JmsBindingMetadata metadata) {
        CacheLevel cacheLevel = metadata.getCacheLevel();
        if (CacheLevel.CONNECTION == cacheLevel) {
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

        if (metadata.getUriMessageSelection() != null) {
            configuration.setMessageSelector(metadata.getUriMessageSelection().getSelector());
        } else if (metadata.getMessageSelection() != null) {
            configuration.setMessageSelector(metadata.getMessageSelection().getSelector());
        }
        //        configuration.setDeliveryMode();
        //        configuration.setDurableSubscriptionName();
        //        configuration.setExceptionListener();
        //        configuration.setLocalDelivery();
    }

    public void detach(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        try {
            containerManager.unregister(target.getUri());
            // FABRICTHREE-544: release must be done after unregistering since a container may attempt to receive a message from a closed connection
            resolver.release(source.getMetadata().getConnectionFactory());
        } catch (JMSException e) {
            throw new ContainerException(e);
        }
    }

    public void attachObjectFactory(JmsWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws ContainerException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws ContainerException {
        throw new AssertionError();
    }

    private ResolvedObjects resolveAdministeredObjects(JmsWireSourceDefinition source) throws ContainerException {
        JmsBindingMetadata metadata = source.getMetadata();
        ConnectionFactoryDefinition requestDefinition = metadata.getConnectionFactory();

        ConnectionFactory requestConnectionFactory = resolver.resolve(requestDefinition);
        DestinationDefinition requestDestinationDefinition = metadata.getDestination();
        Destination requestDestination = resolver.resolve(requestDestinationDefinition, requestConnectionFactory);

        validateDestination(requestDestination, requestDestinationDefinition);

        ConnectionFactory responseConnectionFactory = null;
        Destination responseDestination = null;
        if (metadata.isResponse()) {
            ConnectionFactoryDefinition responseDefinition = metadata.getResponseConnectionFactory();
            responseConnectionFactory = resolver.resolve(responseDefinition);
            DestinationDefinition responseDestinationDefinition = metadata.getResponseDestination();
            if (responseDestinationDefinition != null) {
                // it is legal to omit the response destination, in which case the service must use the JMSReplyTo header from the request message
                responseDestination = resolver.resolve(responseDestinationDefinition, responseConnectionFactory);
                validateDestination(responseDestination, responseDestinationDefinition);
            }
        }
        return new ResolvedObjects(requestConnectionFactory, requestDestination, responseConnectionFactory, responseDestination);
    }

    private void validateDestination(Destination requestDestination, DestinationDefinition requestDestinationDefinition) throws ContainerException {
        DestinationType requestDestinationType = requestDestinationDefinition.geType();
        if (DestinationType.QUEUE == requestDestinationType && !(requestDestination instanceof Queue)) {
            throw new ContainerException("Destination is not a queue: " + requestDestinationDefinition.getName());
        } else if (DestinationType.TOPIC == requestDestinationType && !(requestDestination instanceof Topic)) {
            throw new ContainerException("Destination is not a topic: " + requestDestinationDefinition.getName());
        }
    }

    private WireHolder createWireHolder(Wire wire, JmsWireSourceDefinition source, SessionType trxType) throws ContainerException {
        JmsBindingMetadata metadata = source.getMetadata();
        List<OperationPayloadTypes> types = source.getPayloadTypes();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();
        List<InvocationChainHolder> chainHolders = new ArrayList<>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            OperationPayloadTypes payloadType = resolveOperation(definition.getName(), types);
            if (payloadType == null) {
                throw new ContainerException("Payload type not found for operation: " + definition.getName());
            }
            chainHolders.add(new InvocationChainHolder(chain, payloadType));
        }
        return new WireHolder(chainHolders, correlationScheme, trxType);
    }

    private OperationPayloadTypes resolveOperation(String operationName, List<OperationPayloadTypes> payloadTypes) {
        for (OperationPayloadTypes type : payloadTypes) {
            if (type.getName().equals(operationName)) {
                return type;
            }
        }
        // programming error
        throw new AssertionError("Error resolving operation: " + operationName);
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

    private List<BindingHandler<Message>> createHandlers(JmsWireSourceDefinition source) {
        if (source.getHandlers().isEmpty()) {
            return null;
        }
        List<BindingHandler<Message>> handlers = new ArrayList<>();
        for (PhysicalBindingHandlerDefinition handlerDefinition : source.getHandlers()) {
            BindingHandler<Message> handler = handlerRegistry.createHandler(Message.class, handlerDefinition);
            handlers.add(handler);
        }
        return handlers;
    }

}

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
import org.fabric3.api.binding.jms.model.Destination;
import org.fabric3.api.binding.jms.model.DestinationType;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
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
import org.fabric3.binding.jms.spi.provision.SessionType;
import org.fabric3.spi.container.binding.handler.BindingHandler;
import org.fabric3.spi.container.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.physical.PhysicalDataTypes;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
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
    private MessageContainerFactory containerFactory;
    private MessageContainerManager containerManager;
    private TransformerInterceptorFactory interceptorFactory;
    private ListenerMonitor monitor;
    private BindingHandlerRegistry handlerRegistry;

    public JmsSourceWireAttacher(@Reference AdministeredObjectResolver resolver,
                                 @Reference MessageContainerFactory containerFactory,
                                 @Reference MessageContainerManager containerManager,
                                 @Reference BindingHandlerRegistry handlerRegistry,
                                 @Reference TransformerInterceptorFactory interceptorFactory,
                                 @Monitor ListenerMonitor monitor) {
        this.resolver = resolver;
        this.containerFactory = containerFactory;
        this.containerManager = containerManager;
        this.interceptorFactory = interceptorFactory;
        this.monitor = monitor;
        this.handlerRegistry = handlerRegistry;
    }

    public void attach(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws Fabric3Exception {
        URI serviceUri = target.getUri();
        ClassLoader loader = source.getClassLoader();
        SessionType trxType = source.getSessionType();
        WireHolder wireHolder = createWireHolder(wire, source, target);

        ResolvedObjects objects = resolveAdministeredObjects(source);

        ContainerConfiguration configuration = new ContainerConfiguration();
        ConnectionFactory requestFactory = objects.getRequestFactory();
        javax.jms.Destination requestDestination = objects.getRequestDestination();
        ConnectionFactory responseFactory = objects.getResponseFactory();
        javax.jms.Destination responseDestination = objects.getResponseDestination();

        List<BindingHandler<Message>> handlers = createHandlers(source);
        ServiceListener listener = new ServiceListener(wireHolder, responseDestination, responseFactory, trxType, loader, handlers, monitor);

        configuration.setDestination(requestDestination);
        configuration.setFactory(requestFactory);
        configuration.setMessageListener(listener);
        configuration.setUri(serviceUri);
        configuration.setSessionType(trxType);
        populateConfiguration(configuration, source.getMetadata());

        if (containerManager.isRegistered(serviceUri)) {
            // the wire has changed and it is being reprovisioned
            containerManager.unregister(serviceUri);
        }
        AdaptiveMessageContainer container = containerFactory.create(configuration);
        containerManager.register(container);
    }

    public void detach(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws Fabric3Exception {
        containerManager.unregister(target.getUri());
        // FABRICTHREE-544: release must be done after unregistering since a container may attempt to receive a message from a closed connection
        resolver.release(source.getMetadata().getConnectionFactory());
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

    private ResolvedObjects resolveAdministeredObjects(JmsWireSourceDefinition source) throws Fabric3Exception {
        JmsBindingMetadata metadata = source.getMetadata();
        ConnectionFactoryDefinition requestDefinition = metadata.getConnectionFactory();

        ConnectionFactory requestConnectionFactory = resolver.resolve(requestDefinition);
        Destination destination = metadata.getDestination();
        javax.jms.Destination requestDestination = resolver.resolve(destination, requestConnectionFactory);

        validateDestination(requestDestination, destination);

        ConnectionFactory responseConnectionFactory = null;
        javax.jms.Destination responseDestination = null;
        if (metadata.isResponse()) {
            ConnectionFactoryDefinition responseDefinition = metadata.getResponseConnectionFactory();
            responseConnectionFactory = resolver.resolve(responseDefinition);
            Destination responseDestinationDefinition = metadata.getResponseDestination();
            if (responseDestinationDefinition != null) {
                // it is legal to omit the response destination, in which case the service must use the JMSReplyTo header from the request message
                responseDestination = resolver.resolve(responseDestinationDefinition, responseConnectionFactory);
                validateDestination(responseDestination, responseDestinationDefinition);
            }
        }
        return new ResolvedObjects(requestConnectionFactory, requestDestination, responseConnectionFactory, responseDestination);
    }

    private void validateDestination(javax.jms.Destination requestDestination, Destination requestDestinationDefinition) throws Fabric3Exception {
        DestinationType requestDestinationType = requestDestinationDefinition.geType();
        if (DestinationType.QUEUE == requestDestinationType && !(requestDestination instanceof Queue)) {
            throw new Fabric3Exception("Destination is not a queue: " + requestDestinationDefinition.getName());
        } else if (DestinationType.TOPIC == requestDestinationType && !(requestDestination instanceof Topic)) {
            throw new Fabric3Exception("Destination is not a topic: " + requestDestinationDefinition.getName());
        }
    }

    private WireHolder createWireHolder(Wire wire, JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws Fabric3Exception {
        JmsBindingMetadata metadata = source.getMetadata();
        List<OperationPayloadTypes> types = source.getPayloadTypes();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();
        List<InvocationChainHolder> chainHolders = new ArrayList<>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            OperationPayloadTypes payloadType = resolveOperation(definition.getName(), types);
            if (payloadType == null) {
                throw new Fabric3Exception("Payload type not found for operation: " + definition.getName());
            }

            if (source.getDataTypes().contains(PhysicalDataTypes.JAXB)) {
                addJAXBInterceptor(source, target, definition, chain);
            }

            chainHolders.add(new InvocationChainHolder(chain, payloadType));
        }
        return new WireHolder(chainHolders, correlationScheme);
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
        private javax.jms.Destination requestDestination;
        private javax.jms.Destination responseDestination;

        private ResolvedObjects(ConnectionFactory requestFactory,
                                javax.jms.Destination requestDestination,
                                ConnectionFactory responseFactory,
                                javax.jms.Destination responseDestination) {
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

        public javax.jms.Destination getRequestDestination() {
            return requestDestination;
        }

        public javax.jms.Destination getResponseDestination() {
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

    private void addJAXBInterceptor(JmsWireSourceDefinition source,
                                    PhysicalWireTargetDefinition target,
                                    PhysicalOperationDefinition op,
                                    InvocationChain chain) {
        ClassLoader sourceClassLoader = source.getClassLoader();
        ClassLoader targetClassLoader = target.getClassLoader();
        List<DataType> jaxTypes = DataTypeHelper.createTypes(op, sourceClassLoader);
        Interceptor jaxbInterceptor = interceptorFactory.createInterceptor(op, DataTypeHelper.JAXB_TYPES, jaxTypes, targetClassLoader, sourceClassLoader);
        chain.addInterceptor(jaxbInterceptor);
    }

}

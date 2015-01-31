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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime.broker;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.ZeroMQPubSubBroker;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.handler.PublisherHandler;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.NonReliableQueuedPublisher;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSingleThreadPublisher;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSubscriber;
import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@Service(ZeroMQPubSubBroker.class)
public class ZeroMQPubSubBrokerImpl implements ZeroMQPubSubBroker, Fabric3EventListener<RuntimeStop> {
    private static final JavaType BYTES = new JavaType(byte[].class);
    private static final JavaType TWO_DIMENSIONAL_BYTES = new JavaType(byte[][].class);

    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private AddressCache addressCache;
    private PortAllocator allocator;
    private TransformerHandlerFactory handlerFactory;
    private ZeroMQManagementService managementService;
    private EventService eventService;
    private ExecutorService executorService;
    private HostInfo info;
    private MessagingMonitor monitor;
    private String hostAddress;

    private long pollTimeout = 10000;  // default to 10 seconds

    private Map<String, Subscriber> subscribers = new HashMap<>();
    private Map<String, PublisherHolder> publishers = new HashMap<>();

    public ZeroMQPubSubBrokerImpl(@Reference ContextManager manager,
                                  @Reference AddressCache addressCache,
                                  @Reference PortAllocator allocator,
                                  @Reference TransformerHandlerFactory handlerFactory,
                                  @Reference ZeroMQManagementService managementService,
                                  @Reference EventService eventService,
                                  @Reference(name = "executorService") ExecutorService executorService,
                                  @Reference HostInfo info,
                                  @Monitor MessagingMonitor monitor) throws UnknownHostException {
        this.manager = manager;
        this.addressCache = addressCache;
        this.allocator = allocator;
        this.handlerFactory = handlerFactory;
        this.managementService = managementService;
        this.eventService = eventService;
        this.executorService = executorService;
        this.info = info;
        this.monitor = monitor;
        this.hostAddress = InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Sets the timeout in milliseconds for polling operations.
     *
     * @param timeout the timeout in milliseconds for polling operations
     */
    @Property(required = false)
    public void setPollTimeout(long timeout) {
        this.pollTimeout = timeout;
    }

    /**
     * Sets this host to bind the publisher to.
     *
     * @param host the host
     */
    @Property(required = false)
    public void setHost(String host) {
        this.hostAddress = host;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStop.class, this);
    }

    public void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) throws ContainerException {
        String channelName = metadata.getChannelName();
        Subscriber subscriber = subscribers.get(channelName);
        if (subscriber == null) {
            String id = subscriberId.toString();

            EventStreamHandler head = createSubscriberHandlers(connection, loader);

            // attach the head handler going from the binding transport to connection head handler
            head.setNext(connection.getEventStream().getHeadHandler());

            List<SocketAddress> addresses;

            boolean refresh;
            if (metadata.getSocketAddresses() != null) {
                // socket addresses to connect to are explicitly configured in the binding definition
                refresh = false;
                addresses = new ArrayList<>();
                for (SocketAddressDefinition addressDefinition : metadata.getSocketAddresses()) {
                    Port port = new SpecifiedPort(addressDefinition.getPort());
                    String specifiedHost = addressDefinition.getHost();
                    if ("localhost".equals(specifiedHost)) {
                        specifiedHost = hostAddress;
                    }
                    SocketAddress socketAddress = new SocketAddress("synthetic", "synthetic", "tcp", specifiedHost, port);
                    addresses.add(socketAddress);
                }
            } else {
                // publisher addresses to connect to are not specified in the binding, retrieve them from the federation layer
                refresh = true;
                addresses = addressCache.getActiveAddresses(channelName);
            }
            subscriber = new NonReliableSubscriber(id, manager, addresses, head, metadata, executorService);
            subscriber.incrementConnectionCount();
            subscriber.start();
            if (refresh) {
                // don't subscribe for updates if the sockets are explicitly configured
                addressCache.subscribe(channelName, subscriber);
            }

            subscribers.put(channelName, subscriber);
            managementService.register(channelName, subscriberId, subscriber);
        } else {
            subscriber.incrementConnectionCount();
        }
        String id = subscriberId.getPath().substring(1) + "/" + subscriberId.getFragment();
        monitor.onSubscribe(id);
    }

    public void unsubscribe(URI subscriberId, ZeroMQMetadata metadata) {
        String channelName = metadata.getChannelName();
        Subscriber subscriber = subscribers.get(channelName);
        if (subscriber == null) {
            throw new IllegalStateException("Subscriber not found: " + subscriberId);
        }
        subscriber.decrementConnectionCount();
        if (!subscriber.hasConnections()) {
            subscribers.remove(channelName);
            subscriber.stop();
        }
        managementService.unregister(channelName, subscriberId);
        String id = subscriberId.getPath().substring(1) + "/" + subscriberId.getFragment();
        monitor.onUnsubscribe(id);
    }

    public void connect(String connectionId, ZeroMQMetadata metadata, boolean dedicatedThread, ChannelConnection connection, ClassLoader loader)
            throws ContainerException {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            String runtimeName = info.getRuntimeName();
            String zone = info.getZoneName();
            SocketAddress address;
            List<SocketAddressDefinition> addresses = metadata.getSocketAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                // socket address to bind on is explicitly configured in the binding definition
                if (addresses.size() != 1) {
                    // sanity check
                    throw new ContainerException("Invalid number of socket addresses: " + addresses.size());
                }
                SocketAddressDefinition addressDefinition = addresses.get(0);
                int portDefinition = addressDefinition.getPort();
                Port port = allocator.reserve(channelName, ZMQ, portDefinition);
                String specifiedHost = addressDefinition.getHost();
                if ("localhost".equals(specifiedHost)) {
                    specifiedHost = hostAddress;
                }
                address = new SocketAddress(runtimeName, zone, "tcp", specifiedHost, port);
            } else {
                // socket address to bind on is not configured in the binding definition - allocate one
                Port port = allocator.allocate(channelName, ZMQ);
                address = new SocketAddress(runtimeName, zone, "tcp", hostAddress, port);
            }

            Publisher publisher;
            if (dedicatedThread) {
                publisher = new NonReliableSingleThreadPublisher(manager, address, metadata);
            } else {
                publisher = new NonReliableQueuedPublisher(manager, address, metadata, pollTimeout, monitor);
            }
            attachConnection(connection, publisher, loader);

            AddressAnnouncement event = new AddressAnnouncement(channelName, AddressAnnouncement.Type.ACTIVATED, address);
            addressCache.publish(event);
            publisher.start();

            holder = new PublisherHolder(publisher, address);
            holder.getConnectionIds().add(connectionId);
            publishers.put(channelName, holder);
            managementService.register(channelName, publisher);
        } else {
            Publisher publisher = holder.getPublisher();
            attachConnection(connection, publisher, loader);
            holder.getConnectionIds().add(connectionId);
        }
    }

    public void release(String connectionId, ZeroMQMetadata metadata) throws ContainerException {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            throw new ContainerException("Publisher not found for " + channelName);
        }
        Publisher publisher = holder.getPublisher();
        holder.getConnectionIds().remove(connectionId);
        if (holder.getConnectionIds().isEmpty()) {
            publishers.remove(channelName);
            publisher.stop();

            SocketAddress address = holder.getAddress();
            AddressAnnouncement event = new AddressAnnouncement(channelName, AddressAnnouncement.Type.REMOVED, address);
            addressCache.publish(event);
            managementService.unregister(channelName);
        }
        allocator.release(channelName);
    }

    public void startAll() {
        for (Subscriber subscriber : subscribers.values()) {
            subscriber.start();
        }
        for (PublisherHolder holder : publishers.values()) {
            holder.getPublisher().start();
        }
    }

    public void stopAll() {
        for (Subscriber subscriber : subscribers.values()) {
            subscriber.stop();
        }
        for (PublisherHolder holder : publishers.values()) {
            holder.getPublisher().stop();
        }
    }

    public void onEvent(RuntimeStop event) {
        stopAll();
    }

    private void attachConnection(ChannelConnection connection, Publisher publisher, ClassLoader loader) throws ContainerException {
        EventStream stream = connection.getEventStream();
        try {
            DataType dataType = getEventType(stream, loader);
            EventStreamHandler transformer;
            if (dataType.getType().equals(byte[][].class)) {
                // multi-frame data
                transformer = handlerFactory.createHandler(dataType, TWO_DIMENSIONAL_BYTES, Collections.<Class<?>>emptyList(), loader);
            } else {
                // single frame data
                transformer = handlerFactory.createHandler(dataType, BYTES, Collections.<Class<?>>emptyList(), loader);
            }

            stream.addHandler(transformer);
        } catch (ClassNotFoundException e) {
            throw new ContainerException("Error loading event type", e);
        }
        stream.addHandler(new PublisherHandler(publisher));
    }

    private EventStreamHandler createSubscriberHandlers(ChannelConnection connection, ClassLoader loader) throws ContainerException {
        DataType dataType = getEventTypeForConnection(connection, loader);
        EventStreamHandler head;
        if (dataType.getType().equals(byte[][].class)) {
            // multi-frame data
            head = handlerFactory.createHandler(TWO_DIMENSIONAL_BYTES, dataType, Collections.<Class<?>>emptyList(), loader);
        } else {
            // single frame data
            head = handlerFactory.createHandler(BYTES, dataType, Collections.<Class<?>>emptyList(), loader);
        }
        return head;
    }

    @SuppressWarnings({"unchecked"})
    private DataType getEventType(EventStream stream, ClassLoader loader) throws ClassNotFoundException {
        Class<?> type;
        List<String> eventTypes = stream.getDefinition().getEventTypes();
        if (eventTypes.isEmpty()) {
            // default to Object if there are no event types
            type = Object.class;
        } else {
            type = ParameterTypeHelper.loadClass(eventTypes.get(0), loader);
        }
        return new JavaType(type);
    }

    @SuppressWarnings({"unchecked"})
    private DataType getEventTypeForConnection(ChannelConnection connection, ClassLoader loader) throws ContainerException {
        PhysicalEventStreamDefinition eventStreamDefinition = connection.getEventStream().getDefinition();
        if (!eventStreamDefinition.getEventTypes().isEmpty()) {
            try {
                String eventType = eventStreamDefinition.getEventTypes().get(0);
                Class<?> type = ParameterTypeHelper.loadClass(eventType, loader);
                return new JavaType(type);
            } catch (ClassNotFoundException e) {
                throw new ContainerException(e);
            }
        } else {
            return new JavaType(Object.class);
        }
    }

    private class PublisherHolder {
        private List<String> connectionIds = new ArrayList<>();
        private Publisher publisher;
        private SocketAddress address;

        private PublisherHolder(Publisher publisher, SocketAddress address) {
            this.publisher = publisher;
            this.address = address;
        }

        public List<String> getConnectionIds() {
            return connectionIds;
        }

        public Publisher getPublisher() {
            return publisher;
        }

        public SocketAddress getAddress() {
            return address;
        }
    }

}

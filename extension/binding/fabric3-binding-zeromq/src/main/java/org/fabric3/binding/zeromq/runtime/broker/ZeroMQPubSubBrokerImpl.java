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

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
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
import org.fabric3.spi.discovery.ChannelEntry;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;
import static java.util.stream.Collectors.toList;

/**
 *
 */
@Service(ZeroMQPubSubBroker.class)
public class ZeroMQPubSubBrokerImpl implements ZeroMQPubSubBroker, Fabric3EventListener<RuntimeStop> {
    private static final JavaType BYTES = new JavaType(byte[].class);
    private static final JavaType TWO_DIMENSIONAL_BYTES = new JavaType(byte[][].class);

    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private DiscoveryAgent discoveryAgent;
    private PortAllocator allocator;
    private TransformerHandlerFactory handlerFactory;
    private ZeroMQManagementService managementService;
    private EventService eventService;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private String hostAddress;

    private long pollTimeout = 10000;  // default to 10 seconds

    private Map<String, Subscriber> subscribers = new HashMap<>();
    private Map<String, PublisherHolder> publishers = new HashMap<>();

    public ZeroMQPubSubBrokerImpl(@Reference ContextManager manager,
                                  @Reference(required = false) DiscoveryAgent discoveryAgent,
                                  @Reference PortAllocator allocator,
                                  @Reference TransformerHandlerFactory handlerFactory,
                                  @Reference ZeroMQManagementService managementService,
                                  @Reference EventService eventService,
                                  @Reference(name = "executorService") ExecutorService executorService,
                                  @Monitor MessagingMonitor monitor) throws UnknownHostException {
        this.manager = manager;
        this.discoveryAgent = discoveryAgent;
        this.allocator = allocator;
        this.handlerFactory = handlerFactory;
        this.managementService = managementService;
        this.eventService = eventService;
        this.executorService = executorService;
        this.monitor = monitor;
        this.hostAddress = InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Sets the timeout in milliseconds for polling operations.
     *
     * @param timeout the timeout in milliseconds for polling operations
     */
    @Property(required = false)
    @Source("$systemConfig//f3:zeromq.binding/@poll.timeout")
    public void setPollTimeout(long timeout) {
        this.pollTimeout = timeout;
    }

    /**
     * Sets this host to bind the publisher to.
     *
     * @param host the host
     */
    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@host.address")
    public void setHost(String host) {
        this.hostAddress = host;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStop.class, this);
    }

    public void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) throws Fabric3Exception {
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
                    SocketAddress socketAddress = new SocketAddress("tcp", specifiedHost, port);
                    addresses.add(socketAddress);
                }
            } else {
                // publisher addresses to connect to are not specified in the binding, retrieve them from the federation layer
                if (discoveryAgent == null) {
                    throw new Fabric3Exception("Discovery extension must be installed for dynamic channel addresses");
                }
                refresh = true;
                List<ChannelEntry> entries = discoveryAgent.getChannelEntries(channelName);
                addresses = entries.stream().
                        map(e -> new SocketAddress(e.getTransport(), e.getAddress(), new SpecifiedPort(e.getPort()))).collect(toList());
            }
            subscriber = new NonReliableSubscriber(id, manager, addresses, head, metadata, executorService);
            subscriber.incrementConnectionCount();
            subscriber.start();
            if (refresh && discoveryAgent != null) {
                // don't subscribe for updates if the sockets are explicitly configured
                discoveryAgent.registerChannelListener(channelName, subscriber);
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
        if (discoveryAgent != null) {
            discoveryAgent.unregisterChannelListener(channelName, subscriber);
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
            throws Fabric3Exception {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            SocketAddress address;
            List<SocketAddressDefinition> addresses = metadata.getSocketAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                // socket address to bind on is explicitly configured in the binding definition
                if (addresses.size() != 1) {
                    // sanity check
                    throw new Fabric3Exception("Invalid number of socket addresses: " + addresses.size());
                }
                SocketAddressDefinition addressDefinition = addresses.get(0);
                int portDefinition = addressDefinition.getPort();
                Port port = allocator.reserve(channelName, ZMQ, portDefinition);
                String specifiedHost = addressDefinition.getHost();
                if ("localhost".equals(specifiedHost)) {
                    specifiedHost = hostAddress;
                }
                address = new SocketAddress("tcp", specifiedHost, port);
            } else {
                // socket address to bind on is not configured in the binding definition - allocate one
                Port port = allocator.allocate(channelName, ZMQ);
                address = new SocketAddress("tcp", hostAddress, port);
            }

            Publisher publisher;
            if (dedicatedThread) {
                publisher = new NonReliableSingleThreadPublisher(manager, address, metadata);
            } else {
                publisher = new NonReliableQueuedPublisher(manager, address, metadata, pollTimeout, monitor);
            }
            attachConnection(connection, publisher, loader);

            if (discoveryAgent != null) {
                ChannelEntry entry = new ChannelEntry();
                entry.setName(channelName);
                entry.setAddress(address.getAddress());
                entry.setPort(address.getPort().getNumber());
                entry.setTransport("tcp");

                discoveryAgent.register(entry);
            }

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

    public void release(String connectionId, ZeroMQMetadata metadata) throws Fabric3Exception {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            throw new Fabric3Exception("Publisher not found for " + channelName);
        }
        Publisher publisher = holder.getPublisher();
        holder.getConnectionIds().remove(connectionId);
        if (holder.getConnectionIds().isEmpty()) {
            publishers.remove(channelName);
            publisher.stop();

            if (discoveryAgent != null) {
                discoveryAgent.unregisterChannel(channelName);
            }
            managementService.unregister(channelName);
        }
        allocator.release(channelName);
    }

    public void startAll() {
        subscribers.values().forEach(Subscriber::start);
        for (PublisherHolder holder : publishers.values()) {
            holder.getPublisher().start();
        }
    }

    public void stopAll() {
        subscribers.values().forEach(Subscriber::stop);
        for (PublisherHolder holder : publishers.values()) {
            holder.getPublisher().stop();
        }
    }

    public void onEvent(RuntimeStop event) {
        stopAll();
    }

    private void attachConnection(ChannelConnection connection, Publisher publisher, ClassLoader loader) throws Fabric3Exception {
        EventStream stream = connection.getEventStream();
        DataType dataType = getEventType(stream);
        EventStreamHandler transformer;
        if (dataType.getType().equals(byte[][].class)) {
            // multi-frame data
            transformer = handlerFactory.createHandler(dataType, TWO_DIMENSIONAL_BYTES, Collections.<Class<?>>emptyList(), loader);
        } else {
            // single frame data
            transformer = handlerFactory.createHandler(dataType, BYTES, Collections.<Class<?>>emptyList(), loader);
        }

        stream.addHandler(transformer);
        stream.addHandler(new PublisherHandler(publisher));
    }

    private EventStreamHandler createSubscriberHandlers(ChannelConnection connection, ClassLoader loader) throws Fabric3Exception {
        DataType dataType = getEventType(connection.getEventStream());
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
    private DataType getEventType(EventStream stream) {
        return new JavaType(stream.getEventType());
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

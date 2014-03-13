/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.broker;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.BrokerException;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.ZeroMQPubSubBroker;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.binding.zeromq.runtime.handler.PublisherHandler;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.NonReliableQueuedPublisher;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSingleThreadPublisher;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSubscriber;
import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.HandlerCreationException;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeStop;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;
import org.fabric3.spi.model.type.java.JavaClass;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@Service(ZeroMQPubSubBroker.class)
public class ZeroMQPubSubBrokerImpl implements ZeroMQPubSubBroker, Fabric3EventListener<RuntimeStop> {
    private static final DataType BYTES = new JavaClass<>(byte[].class);
    private static final DataType TWO_DIMENSIONAL_BYTES = new JavaClass<>(byte[][].class);

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
                                  @Reference ExecutorService executorService,
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

    public void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) throws BrokerException {
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
            throws BrokerException {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            try {
                String runtimeName = info.getRuntimeName();
                String zone = info.getZoneName();
                SocketAddress address;
                List<SocketAddressDefinition> addresses = metadata.getSocketAddresses();
                if (addresses != null && !addresses.isEmpty()) {
                    // socket address to bind on is explicitly configured in the binding definition
                    if (addresses.size() != 1) {
                        // sanity check
                        throw new BrokerException("Invalid number of socket addresses: " + addresses.size());
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
            } catch (PortAllocationException e) {
                throw new BrokerException("Error creating connection to " + channelName, e);
            }
        } else {
            Publisher publisher = holder.getPublisher();
            attachConnection(connection, publisher, loader);
            holder.getConnectionIds().add(connectionId);
        }
    }

    public void release(String connectionId, ZeroMQMetadata metadata) throws BrokerException {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            throw new BrokerException("Publisher not found for " + channelName);
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

    private void attachConnection(ChannelConnection connection, Publisher publisher, ClassLoader loader) throws BrokerException {
        EventStream stream = connection.getEventStream();
        try {
            DataType dataType = getEventType(stream, loader);
            EventStreamHandler transformer;
            if (dataType.getPhysical().equals(byte[][].class)) {
                // multi-frame data
                transformer = handlerFactory.createHandler(dataType, TWO_DIMENSIONAL_BYTES, loader);
            } else {
                // single frame data
                transformer = handlerFactory.createHandler(dataType, BYTES, loader);
            }

            stream.addHandler(transformer);
        } catch (ClassNotFoundException e) {
            throw new BrokerException("Error loading event type", e);
        } catch (HandlerCreationException e) {
            throw new BrokerException(e);
        }
        stream.addHandler(new PublisherHandler(publisher));
    }

    private EventStreamHandler createSubscriberHandlers(ChannelConnection connection, ClassLoader loader) throws BrokerException {
        try {
            DataType dataType = getEventTypeForConnection(connection, loader);
            EventStreamHandler head;
            if (dataType.getPhysical().equals(byte[][].class)) {
                // multi-frame data
                head = handlerFactory.createHandler(TWO_DIMENSIONAL_BYTES, dataType, loader);
            } else {
                // single frame data
                head = handlerFactory.createHandler(BYTES, dataType, loader);
            }
            return head;
        } catch (HandlerCreationException e) {
            throw new BrokerException(e);
        }
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
        return new JavaClass(type);
    }

    @SuppressWarnings({"unchecked"})
    private DataType getEventTypeForConnection(ChannelConnection connection, ClassLoader loader) throws BrokerException {
        PhysicalEventStreamDefinition eventStreamDefinition = connection.getEventStream().getDefinition();
        if (!eventStreamDefinition.getEventTypes().isEmpty()) {
            try {
                String eventType = eventStreamDefinition.getEventTypes().get(0);
                Class<?> type = ParameterTypeHelper.loadClass(eventType, loader);
                return new JavaClass(type);
            } catch (ClassNotFoundException e) {
                throw new BrokerException(e);
            }
        } else {
            return new JavaClass<>(Object.class);
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

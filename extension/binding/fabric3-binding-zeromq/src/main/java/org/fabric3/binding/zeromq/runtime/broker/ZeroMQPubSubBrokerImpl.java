/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.zeromq.ZMQ;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.BrokerException;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.ZeroMQPubSubBroker;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressAnnouncement;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.handler.AsyncFanOutHandler;
import org.fabric3.binding.zeromq.runtime.handler.DeserializingEventStreamHandler;
import org.fabric3.binding.zeromq.runtime.handler.PublisherHandler;
import org.fabric3.binding.zeromq.runtime.handler.SerializingEventStreamHandler;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.NonReliablePublisher;
import org.fabric3.binding.zeromq.runtime.message.NonReliableSubscriber;
import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class ZeroMQPubSubBrokerImpl implements ZeroMQPubSubBroker {
    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private AddressCache addressCache;
    private ExecutorService executorService;
    private PortAllocator allocator;
    private ZeroMQManagementService managementService;
    private HostInfo info;
    private MessagingMonitor monitor;

    private long pollTimeout = 1000;

    private Map<String, Subscriber> subscribers = new HashMap<String, Subscriber>();
    private Map<String, PublisherHolder> publishers = new HashMap<String, PublisherHolder>();

    public ZeroMQPubSubBrokerImpl(@Reference ContextManager manager,
                                  @Reference AddressCache addressCache,
                                  @Reference ExecutorService executorService,
                                  @Reference PortAllocator allocator,
                                  @Reference ZeroMQManagementService managementService,
                                  @Reference HostInfo info,
                                  @Monitor MessagingMonitor monitor) {
        this.manager = manager;
        this.addressCache = addressCache;
        this.executorService = executorService;
        this.allocator = allocator;
        this.managementService = managementService;
        this.info = info;
        this.monitor = monitor;
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

    public void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) {
        String channelName = metadata.getChannelName();
        Subscriber subscriber = subscribers.get(channelName);
        if (subscriber == null) {
            AsyncFanOutHandler fanOutHandler = new AsyncFanOutHandler(executorService);
            fanOutHandler.addConnection(subscriberId, connection);

            DeserializingEventStreamHandler head = new DeserializingEventStreamHandler(loader);
            head.setNext(fanOutHandler);

            List<SocketAddress> addresses = addressCache.getActiveAddresses(channelName);
            ZMQ.Context context = manager.getContext();
            subscriber = new NonReliableSubscriber(subscriberId.toString(), context, addresses, head, metadata, monitor);
            subscriber.start();
            addressCache.subscribe(channelName, subscriber);
            subscribers.put(channelName, subscriber);
            managementService.register(channelName, subscriberId, subscriber);
        } else {
            subscriber.addConnection(subscriberId, connection);
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
        subscriber.removeConnection(subscriberId);
        if (!subscriber.hasConnections()) {
            subscribers.remove(channelName);
            subscriber.stop();
        }
        managementService.unregister(channelName, subscriberId);
        String id = subscriberId.getPath().substring(1) + "/" + subscriberId.getFragment();
        monitor.onUnsubscribe(id);
    }

    public void connect(String connectionId, ChannelConnection connection, ZeroMQMetadata metadata) throws BrokerException {
        String channelName = metadata.getChannelName();
        PublisherHolder holder = publishers.get(channelName);
        if (holder == null) {
            try {
                Port port = allocator.allocate(channelName, ZMQ);
                // XCV FIXME localhost
                String runtimeName = info.getRuntimeName();
                SocketAddress address = new SocketAddress(runtimeName, "tcp", InetAddress.getLocalHost().getHostAddress(), port);
                ZMQ.Context context = manager.getContext();

                Publisher publisher = new NonReliablePublisher(context, address, pollTimeout, metadata, monitor);
                attachConnection(connection, publisher);

                AddressAnnouncement event = new AddressAnnouncement(channelName, AddressAnnouncement.Type.ACTIVATED, address);
                addressCache.publish(event);
                publisher.start();

                holder = new PublisherHolder(publisher);
                holder.getConnectionIds().add(connectionId);
                publishers.put(channelName, holder);
                managementService.register(channelName, publisher);
            } catch (PortAllocationException e) {
                throw new BrokerException("Error creating connection to " + channelName, e);
            } catch (UnknownHostException e) {
                throw new BrokerException("Error creating connection to " + channelName, e);
            }
        } else {
            Publisher publisher = holder.getPublisher();
            attachConnection(connection, publisher);
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
            publishers.remove(connectionId);
            publisher.stop();
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

    private void attachConnection(ChannelConnection connection, Publisher publisher) {
        for (EventStream stream : connection.getEventStreams()) {
            stream.addHandler(new SerializingEventStreamHandler());
            stream.addHandler(new PublisherHandler(publisher));
        }
    }

    private class PublisherHolder {
        private List<String> connectionIds = new ArrayList<String>();
        private Publisher publisher;

        private PublisherHolder(Publisher publisher) {
            this.publisher = publisher;
        }

        public List<String> getConnectionIds() {
            return connectionIds;
        }

        public Publisher getPublisher() {
            return publisher;
        }
    }


}

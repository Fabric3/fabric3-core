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
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.binding.zeromq.runtime.BrokerException;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.ZeroMQWireBroker;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.interceptor.OneWayInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.RequestReplyInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.UnwrappingInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.WrappingInterceptor;
import org.fabric3.binding.zeromq.runtime.management.ZeroMQManagementService;
import org.fabric3.binding.zeromq.runtime.message.DelegatingOneWaySender;
import org.fabric3.binding.zeromq.runtime.message.DynamicOneWaySender;
import org.fabric3.binding.zeromq.runtime.message.NonReliableOneWayReceiver;
import org.fabric3.binding.zeromq.runtime.message.NonReliableOneWaySender;
import org.fabric3.binding.zeromq.runtime.message.NonReliableRequestReplyReceiver;
import org.fabric3.binding.zeromq.runtime.message.NonReliableRequestReplySender;
import org.fabric3.binding.zeromq.runtime.message.OneWaySender;
import org.fabric3.binding.zeromq.runtime.message.Receiver;
import org.fabric3.binding.zeromq.runtime.message.RequestReplySender;
import org.fabric3.binding.zeromq.runtime.message.Sender;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InterceptorCreationException;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.federation.addressing.AddressAnnouncement;
import org.fabric3.spi.federation.addressing.AddressCache;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
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
@Service(ZeroMQWireBroker.class)
public class ZeroMQWireBrokerImpl implements ZeroMQWireBroker, DynamicOneWaySender, Fabric3EventListener<RuntimeStop> {
    private static final JavaType BYTE_TYPE = new JavaType(byte[].class);
    private static final JavaType EMPTY_TYPE = new JavaType(Void.class);
    List<DataType> TRANSPORT_TYPES;   // the transport type is a byte array

    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private AddressCache addressCache;
    private PortAllocator allocator;
    private EventService eventService;
    private HostInfo info;
    private ZeroMQManagementService managementService;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private long pollTimeout = 10000000;
    private TransformerInterceptorFactory interceptorFactory;
    private String host;
    private String hostAddress;

    private Map<String, SenderHolder> senders = new HashMap<>();
    private Map<String, Receiver> receivers = new HashMap<>();

    public ZeroMQWireBrokerImpl(@Reference ContextManager manager,
                                @Reference AddressCache addressCache,
                                @Reference PortAllocator allocator,
                                @Reference(name = "executorService") ExecutorService executorService,
                                @Reference ZeroMQManagementService managementService,
                                @Reference EventService eventService,
                                @Reference TransformerInterceptorFactory interceptorFactory,
                                @Reference HostInfo info,
                                @Monitor MessagingMonitor monitor) throws UnknownHostException {
        this.manager = manager;
        this.addressCache = addressCache;
        this.allocator = allocator;
        this.executorService = executorService;
        this.managementService = managementService;
        this.eventService = eventService;
        this.interceptorFactory = interceptorFactory;
        this.info = info;
        this.monitor = monitor;
        this.host = InetAddress.getLocalHost().getHostAddress();
        TRANSPORT_TYPES = new ArrayList<>();
        TRANSPORT_TYPES.add(BYTE_TYPE);
        this.hostAddress = InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Sets the timeout in milliseconds for polling operations.
     *
     * @param timeout the timeout in milliseconds for polling operations
     */
    @Property(required = false)
    public void setPollTimeout(long timeout) {
        this.pollTimeout = timeout * 1000; // convert milliseconds to microseconds
    }

    /**
     * Sets this host to bind the publisher to.
     *
     * @param host the host
     */
    @Property(required = false)
    public void setHost(String host) {
        this.host = host;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStop.class, this);
    }

    public void connectToSender(String id, URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws BrokerException {
        SenderHolder holder;
        if (ZMQ.equals(uri.getScheme())) {
            DelegatingOneWaySender sender = new DelegatingOneWaySender(id, this, metadata);
            holder = new SenderHolder(sender);
        } else {
            holder = senders.get(uri.toString());
        }
        if (holder == null) {
            boolean oneWay = isOneWay(chains, uri);
            holder = createSender(uri.toString(), oneWay, metadata);
            managementService.registerSender(id, holder.getSender());
        }
        for (int i = 0, chainsSize = chains.size(); i < chainsSize; i++) {
            InvocationChain chain = chains.get(i);
            try {
                PhysicalOperationDefinition physicalOperation = chain.getPhysicalOperation();
                List<DataType> sourceTypes = createTypes(physicalOperation, loader);
                Interceptor interceptor = interceptorFactory.createInterceptor(physicalOperation, sourceTypes, TRANSPORT_TYPES, loader, loader);
                chain.addInterceptor(interceptor);
                chain.addInterceptor(new UnwrappingInterceptor());
            } catch (InterceptorCreationException e) {
                throw new BrokerException(e);
            }
            Interceptor interceptor = createInterceptor(holder, i);
            chain.addInterceptor(interceptor);
        }
        holder.getIds().add(id);
    }

    public void releaseSender(String id, URI uri) throws BrokerException {
        SenderHolder holder = senders.get(uri.toString());
        if (holder == null) {
            if (!ZMQ.equals(uri.getScheme())) {
                // callback holders are dynamically created and it is possible for a sender to be released before an invocation is dispatched to it
                throw new BrokerException("Sender not found for " + uri);
            } else {
                return;
            }
        }
        holder.getIds().remove(id);
        if (holder.getIds().isEmpty()) {
            senders.remove(uri.toString());
            Sender sender = holder.getSender();
            sender.stop();
            managementService.unregisterSender(id);
        }
    }

    public void connectToReceiver(URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws BrokerException {
        if (receivers.containsKey(uri.toString())) {
            throw new BrokerException("Receiver already defined for " + uri);
        }
        try {
            String endpointId = uri.toString();

            String runtimeName = info.getRuntimeName();
            String zone = info.getZoneName();
            SocketAddress address;

            if (metadata.getSocketAddresses() != null && !metadata.getSocketAddresses().isEmpty()) {
                // bind using specified address and port
                if (metadata.getSocketAddresses().size() != 1) {
                    throw new BrokerException("Only one socket address can be specified");
                }
                SocketAddressDefinition addressDefinition = metadata.getSocketAddresses().get(0);
                String specifiedHost = addressDefinition.getHost();
                if ("localhost".equals(specifiedHost)) {
                    specifiedHost = hostAddress;
                }
                int portNumber = addressDefinition.getPort();
                Port port = allocator.reserve(endpointId, ZMQ, portNumber);
                address = new SocketAddress(runtimeName, zone, "tcp", specifiedHost, port);
            } else {
                // bind to a randomly allocated port
                Port port = allocator.allocate(endpointId, ZMQ);
                address = new SocketAddress(runtimeName, zone, "tcp", host, port);
            }

            addTransformer(chains, loader);

            boolean oneWay = isOneWay(chains, uri);
            Receiver receiver;
            if (oneWay) {
                receiver = new NonReliableOneWayReceiver(manager, address, chains, executorService, metadata, monitor);
            } else {
                receiver = new NonReliableRequestReplyReceiver(manager, address, chains, executorService, pollTimeout, metadata, monitor);
            }
            receiver.start();

            AddressAnnouncement event = new AddressAnnouncement(endpointId, AddressAnnouncement.Type.ACTIVATED, address);
            addressCache.publish(event);

            receivers.put(uri.toString(), receiver);
            String id = createReceiverId(uri);
            managementService.registerReceiver(id, receiver);
            monitor.onProvisionEndpoint(id);
        } catch (PortAllocationException e) {
            throw new BrokerException("Error allocating port for " + uri, e);
        }
    }

    public void releaseReceiver(URI uri) throws BrokerException {
        Receiver receiver = receivers.remove(uri.toString());
        if (receiver == null) {
            throw new BrokerException("Receiver not found for " + uri);
        }
        String endpointId = uri.toString();

        SocketAddress address = receiver.getAddress();
        AddressAnnouncement event = new AddressAnnouncement(endpointId, AddressAnnouncement.Type.REMOVED, address);
        addressCache.publish(event);

        receiver.stop();
        allocator.release(endpointId);
        String id = createReceiverId(uri);
        managementService.unregisterReceiver(id);
        monitor.onRemoveEndpoint(id);
    }

    public void send(byte[] message, int index, WorkContext context, ZeroMQMetadata metadata) {
        CallbackReference callbackReference = context.peekCallbackReference();
        if (callbackReference == null) {
            monitor.error("Callback reference not found");
            return;
        }
        String callback = callbackReference.getServiceUri();
        SenderHolder holder = senders.get(callback);
        if (holder == null) {
            holder = createSender(callback, true, metadata);
        }
        Sender sender = holder.getSender();
        if (sender instanceof OneWaySender) {
            ((OneWaySender) sender).send(message, index, context);
        } else {
            monitor.error("Callback sender is not a one-way type: " + holder.getClass().getName());
        }
    }

    public void startAll() {
        for (Receiver receiver : receivers.values()) {
            receiver.start();
        }
        for (SenderHolder holder : senders.values()) {
            holder.getSender().start();
        }
    }

    public void stopAll() {
        for (Receiver receiver : receivers.values()) {
            receiver.stop();
        }
        for (SenderHolder holder : senders.values()) {
            holder.getSender().stop();
        }
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

    public String getId() {
        return "ZeroMQWireBroker";
    }

    public void onUpdate(List<SocketAddress> addresses) {
        // no-op
    }

    public void onEvent(RuntimeStop event) {
        stopAll();
    }

    private SenderHolder createSender(String endpointId, boolean oneWay, ZeroMQMetadata metadata) {
        List<SocketAddress> addresses = new ArrayList<>();
        boolean refresh;
        if (metadata.getSocketAddresses() != null) {
            // service addresses to connect to are explicitly configured in the binding definition
            refresh = false;

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
            // services addresses to connect to are not specified in the binding, retrieve them from the federation layer
            refresh = true;
            addresses = addressCache.getActiveAddresses(endpointId);
        }

        Sender sender;
        if (oneWay) {
            sender = new NonReliableOneWaySender(endpointId, manager, addresses, pollTimeout, metadata, monitor);
        } else {
            sender = new NonReliableRequestReplySender(endpointId, manager, addresses, pollTimeout, metadata, monitor);
        }
        SenderHolder holder = new SenderHolder(sender);
        sender.start();

        if (refresh) {
            // don't subscribe for updates if the sockets are explicitly configured
            addressCache.subscribe(endpointId, sender);
        }

        senders.put(endpointId, holder);
        return holder;
    }

    /**
     * Determines if the wire is one-way or request-reply. The first operation is used to determine if the contract is one-way as the binding does not support
     * mixing one-way and request-response operations on a service contract.
     *
     * @param chains the wire invocation chains
     * @param uri    thr service URI.
     * @return true if the wire is one-way
     */
    private boolean isOneWay(List<InvocationChain> chains, URI uri) {
        if (chains.size() < 1) {
            throw new AssertionError("Contract must have at least one operation: " + uri);
        }
        return chains.get(0).getPhysicalOperation().isOneWay();
    }

    private void addTransformer(List<InvocationChain> chains, ClassLoader loader) throws BrokerException {
        for (InvocationChain chain : chains) {
            try {
                PhysicalOperationDefinition physicalOperation = chain.getPhysicalOperation();
                List<DataType> targetTypes = createTypes(physicalOperation, loader);
                Interceptor interceptor = interceptorFactory.createInterceptor(physicalOperation, TRANSPORT_TYPES, targetTypes, loader, loader);
                chain.addInterceptor(new WrappingInterceptor());
                chain.addInterceptor(interceptor);
            } catch (InterceptorCreationException e) {
                throw new BrokerException(e);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<DataType> createTypes(PhysicalOperationDefinition physicalOperation, ClassLoader loader) throws BrokerException {
        try {
            List<DataType> dataTypes = new ArrayList<>();
            if (physicalOperation.getSourceParameterTypes().isEmpty()) {
                // no params
                dataTypes.add(EMPTY_TYPE);
            } else {
                List<Class<?>> types = ParameterTypeHelper.loadSourceInParameterTypes(physicalOperation, loader);
                for (Class<?> type : types) {
                    dataTypes.add(new JavaType((type)));
                }
            }
            return dataTypes;
        } catch (ClassNotFoundException e) {
            throw new BrokerException("Error transforming parameter", e);
        }
    }

    private Interceptor createInterceptor(SenderHolder holder, int i) {
        Sender sender = holder.getSender();
        if (sender instanceof NonReliableRequestReplySender) {
            return new RequestReplyInterceptor(i, (RequestReplySender) sender);
        } else if (sender instanceof OneWaySender) {
            return new OneWayInterceptor(i, (OneWaySender) sender);
        } else {
            throw new AssertionError("Unknown sender type: " + sender.getClass().getName());
        }
    }

    private String createReceiverId(URI uri) {
        if ("zmq".equals(uri.getScheme())) {
            // callback ids are of the form zmq://<service>
            return uri.getAuthority();
        }
        return uri.getPath().substring(1) + "/" + uri.getFragment();
    }

    private class SenderHolder {
        private Sender sender;
        private List<String> ids;

        private SenderHolder(Sender sender) {
            this.sender = sender;
            ids = new ArrayList<>();
        }

        public Sender getSender() {
            return sender;
        }

        public List<String> getIds() {
            return ids;
        }
    }

}

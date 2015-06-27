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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
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
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.TransformerInterceptorFactory;
import org.fabric3.spi.discovery.DiscoveryAgent;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.model.physical.PhysicalOperation;
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
@Service(ZeroMQWireBroker.class)
public class ZeroMQWireBrokerImpl implements ZeroMQWireBroker, DynamicOneWaySender, Fabric3EventListener<RuntimeStop> {
    private static final JavaType BYTE_TYPE = new JavaType(byte[].class);
    private static final JavaType EMPTY_TYPE = new JavaType(Void.class);
    List<DataType> TRANSPORT_TYPES;   // the transport type is a byte array

    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private DiscoveryAgent discoveryAgent;
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
                                @Reference(required = false) DiscoveryAgent discoveryAgent,
                                @Reference PortAllocator allocator,
                                @Reference(name = "executorService") ExecutorService executorService,
                                @Reference ZeroMQManagementService managementService,
                                @Reference EventService eventService,
                                @Reference TransformerInterceptorFactory interceptorFactory,
                                @Reference HostInfo info,
                                @Monitor MessagingMonitor monitor) throws UnknownHostException {
        this.manager = manager;
        this.discoveryAgent = discoveryAgent;
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
    @Source("$systemConfig//f3:zeromq.binding/@poll.timeout")
    public void setPollTimeout(long timeout) {
        this.pollTimeout = timeout * 1000; // convert milliseconds to microseconds
    }

    /**
     * Sets this host to bind the publisher to.
     *
     * @param host the host
     */
    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@host.address")
    public void setHost(String host) {
        this.host = host;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStop.class, this);
    }

    public void connectToSender(String id, URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws Fabric3Exception {
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
            PhysicalOperation physicalOperation = chain.getPhysicalOperation();
            List<DataType> sourceTypes = createTypes(physicalOperation);
            Interceptor interceptor = interceptorFactory.createInterceptor(physicalOperation, sourceTypes, TRANSPORT_TYPES, loader, loader);
            chain.addInterceptor(interceptor);
            chain.addInterceptor(new UnwrappingInterceptor());
            interceptor = createInterceptor(holder, i);
            chain.addInterceptor(interceptor);
        }
        holder.getIds().add(id);
    }

    public void releaseSender(String id, URI uri) throws Fabric3Exception {
        SenderHolder holder = senders.get(uri.toString());
        if (holder == null) {
            if (!ZMQ.equals(uri.getScheme())) {
                // callback holders are dynamically created and it is possible for a sender to be released before an invocation is dispatched to it
                throw new Fabric3Exception("Sender not found for " + uri);
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

    public void connectToReceiver(URI uri, List<InvocationChain> chains, ZeroMQMetadata metadata, ClassLoader loader) throws Fabric3Exception {
        if (receivers.containsKey(uri.toString())) {
            throw new Fabric3Exception("Receiver already defined for " + uri);
        }
        String endpointId = uri.toString();

        SocketAddress address;

        if (metadata.getSocketAddresses() != null && !metadata.getSocketAddresses().isEmpty()) {
            // bind using specified address and port
            if (metadata.getSocketAddresses().size() != 1) {
                throw new Fabric3Exception("Only one socket address can be specified");
            }
            SocketAddressDefinition addressDefinition = metadata.getSocketAddresses().get(0);
            String specifiedHost = addressDefinition.getHost();
            if ("localhost".equals(specifiedHost)) {
                specifiedHost = hostAddress;
            }
            int portNumber = addressDefinition.getPort();
            Port port = allocator.reserve(endpointId, ZMQ, portNumber);
            address = new SocketAddress("tcp", specifiedHost, port);
        } else {
            // bind to a randomly allocated port
            Port port = allocator.allocate(endpointId, ZMQ);
            address = new SocketAddress("tcp", host, port);
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

        ServiceEntry entry = new ServiceEntry();
        entry.setName(endpointId);
        entry.setAddress(address.getAddress());
        entry.setPort(address.getPort().getNumber());
        entry.setTransport("tcp");

        if (discoveryAgent != null) {
            discoveryAgent.register(entry);
        }
        receivers.put(uri.toString(), receiver);
        String id = createReceiverId(uri);
        managementService.registerReceiver(id, receiver);
        monitor.onProvisionEndpoint(id);
    }

    public void releaseReceiver(URI uri) throws Fabric3Exception {
        Receiver receiver = receivers.remove(uri.toString());
        if (receiver == null) {
            throw new Fabric3Exception("Receiver not found for " + uri);
        }
        String endpointId = uri.toString();

        if (discoveryAgent != null) {
            discoveryAgent.unregisterService(endpointId);
        }
        receiver.stop();
        allocator.release(endpointId);
        String id = createReceiverId(uri);
        managementService.unregisterReceiver(id);
        monitor.onRemoveEndpoint(id);
    }

    public void send(byte[] message, int index, WorkContext context, ZeroMQMetadata metadata) {
        String callbackReference = context.peekCallbackReference();
        if (callbackReference == null) {
            monitor.error("Callback reference not found");
            return;
        }
        SenderHolder holder = senders.get(callbackReference);
        if (holder == null) {
            holder = createSender(callbackReference, true, metadata);
        }
        Sender sender = holder.getSender();
        if (sender instanceof OneWaySender) {
            ((OneWaySender) sender).send(message, index, context);
        } else {
            monitor.error("Callback sender is not a one-way type: " + holder.getClass().getName());
        }
    }

    public void startAll() {
        receivers.values().forEach(Receiver::start);
        for (SenderHolder holder : senders.values()) {
            holder.getSender().start();
        }
    }

    public void stopAll() {
        receivers.values().forEach(Receiver::stop);
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

    public void accept(EntryChange change, ServiceEntry serviceEntry) {
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
                SocketAddress socketAddress = new SocketAddress("tcp", specifiedHost, port);
                addresses.add(socketAddress);
            }

        } else {
            // services addresses to connect to are not specified in the binding, retrieve them from the federation layer
            refresh = true;
            if (discoveryAgent == null) {
                throw new Fabric3Exception("Discovery extension must be installed for dynamic wire addresses");
            }
            List<ServiceEntry> entries = discoveryAgent.getServiceEntries(endpointId);
            addresses = entries.stream().
                    map(e -> new SocketAddress(e.getTransport(), e.getAddress(), new SpecifiedPort(e.getPort()))).collect(toList());
        }

        Sender sender;
        if (oneWay) {
            sender = new NonReliableOneWaySender(endpointId, manager, addresses, pollTimeout, metadata, monitor);
        } else {
            sender = new NonReliableRequestReplySender(endpointId, manager, addresses, pollTimeout, metadata, monitor);
        }
        SenderHolder holder = new SenderHolder(sender);
        sender.start();

        if (refresh && discoveryAgent != null) {
            // don't subscribe for updates if the sockets are explicitly configured
            discoveryAgent.registerServiceListener(endpointId, sender);
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

    private void addTransformer(List<InvocationChain> chains, ClassLoader loader) throws Fabric3Exception {
        for (InvocationChain chain : chains) {
            PhysicalOperation physicalOperation = chain.getPhysicalOperation();
            List<DataType> targetTypes = createTypes(physicalOperation);
            Interceptor interceptor = interceptorFactory.createInterceptor(physicalOperation, TRANSPORT_TYPES, targetTypes, loader, loader);
            chain.addInterceptor(new WrappingInterceptor());
            chain.addInterceptor(interceptor);
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<DataType> createTypes(PhysicalOperation physicalOperation) throws Fabric3Exception {
        List<DataType> dataTypes = new ArrayList<>();
        if (physicalOperation.getSourceParameterTypes().isEmpty()) {
            // no params
            dataTypes.add(EMPTY_TYPE);
        } else {
            List<Class<?>> types = physicalOperation.getSourceParameterTypes();
            dataTypes.addAll(types.stream().map(type -> new JavaType((type))).collect(Collectors.toList()));
        }
        return dataTypes;
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

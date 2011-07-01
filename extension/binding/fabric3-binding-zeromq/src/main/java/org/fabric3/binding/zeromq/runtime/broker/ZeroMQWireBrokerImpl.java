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
import org.osoa.sca.annotations.Service;
import org.zeromq.ZMQ;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.zeromq.runtime.BrokerException;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.ZeroMQWireBroker;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.federation.AddressAnnouncement;
import org.fabric3.binding.zeromq.runtime.federation.AddressCache;
import org.fabric3.binding.zeromq.runtime.interceptor.OneWayInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.ReferenceMarshallingInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.RequestReplyInterceptor;
import org.fabric3.binding.zeromq.runtime.interceptor.ServiceMarshallingInterceptor;
import org.fabric3.binding.zeromq.runtime.message.DelegatingOneWaySender;
import org.fabric3.binding.zeromq.runtime.message.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.message.NonReliableOneWayReceiver;
import org.fabric3.binding.zeromq.runtime.message.NonReliableOneWaySender;
import org.fabric3.binding.zeromq.runtime.message.NonReliableRequestReplyReceiver;
import org.fabric3.binding.zeromq.runtime.message.NonReliableRequestReplySender;
import org.fabric3.binding.zeromq.runtime.message.OneWaySender;
import org.fabric3.binding.zeromq.runtime.message.Receiver;
import org.fabric3.binding.zeromq.runtime.message.RequestReplySender;
import org.fabric3.binding.zeromq.runtime.message.Sender;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
import org.fabric3.spi.host.PortAllocator;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
@Service(ZeroMQWireBroker.class)
public class ZeroMQWireBrokerImpl implements ZeroMQWireBroker, OneWaySender {
    private static final String ZMQ = "zmq";

    private ContextManager manager;
    private AddressCache addressCache;
    private PortAllocator allocator;
    private HostInfo info;
    private ExecutorService executorService;
    private MessagingMonitor monitor;
    private long pollTimeout = 1000;

    private Map<String, SenderHolder> senders = new HashMap<String, SenderHolder>();
    private Map<String, Receiver> receivers = new HashMap<String, Receiver>();

    public ZeroMQWireBrokerImpl(@Reference ContextManager manager,
                                @Reference AddressCache addressCache,
                                @Reference PortAllocator allocator,
                                @Reference ExecutorService executorService,
                                @Reference HostInfo info,
                                @Monitor MessagingMonitor monitor) {
        this.manager = manager;
        this.addressCache = addressCache;
        this.allocator = allocator;
        this.executorService = executorService;
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

    public void connectToSender(String id, URI uri, List<InvocationChain> chains, ClassLoader loader) throws BrokerException {
        SenderHolder holder;
        if (ZMQ.equals(uri.getScheme())) {
            DelegatingOneWaySender sender = new DelegatingOneWaySender(id, this);
            holder = new SenderHolder(sender);
        } else {
            holder = senders.get(uri.toString());
        }
        if (holder == null) {
            boolean oneWay = isOneWay(chains, uri);
            holder = createSender(uri.toString(), oneWay);
        }
        for (int i = 0, chainsSize = chains.size(); i < chainsSize; i++) {
            InvocationChain chain = chains.get(i);
            ReferenceMarshallingInterceptor serializingInterceptor = new ReferenceMarshallingInterceptor(loader);
            chain.addInterceptor(serializingInterceptor);
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
            }
        }
        holder.getIds().remove(id);
        if (holder.getIds().isEmpty()) {
            senders.remove(uri.toString());
            Sender sender = holder.getSender();
            sender.stop();
        }
    }

    public void connectToReceiver(URI uri, List<InvocationChain> chains, ClassLoader loader) throws BrokerException {
        if (receivers.containsKey(uri.toString())) {
            throw new BrokerException("Receiver already defined for " + uri);
        }
        try {
            ZMQ.Context context = manager.getContext();
            String endpointId = uri.toString();

            Port port = allocator.allocate(endpointId, ZMQ);
            // XCV FIXME localhost
            String runtimeName = info.getRuntimeName();
            SocketAddress address = new SocketAddress(runtimeName, "tcp", InetAddress.getLocalHost().getHostAddress(), port);

            for (InvocationChain chain : chains) {
                ServiceMarshallingInterceptor interceptor = new ServiceMarshallingInterceptor(loader);
                chain.addInterceptor(interceptor);
            }
            boolean oneWay = isOneWay(chains, uri);
            Receiver receiver;
            if (oneWay) {
                receiver = new NonReliableOneWayReceiver(context, address, chains, executorService, monitor);
            } else {
                receiver = new NonReliableRequestReplyReceiver(context, address, chains, executorService, pollTimeout, monitor);
            }
            receiver.start();

            AddressAnnouncement event = new AddressAnnouncement(endpointId, AddressAnnouncement.Type.ACTIVATED, address);
            addressCache.publish(event);

            receivers.put(uri.toString(), receiver);
        } catch (PortAllocationException e) {
            throw new BrokerException("Error allocating port for " + uri, e);
        } catch (UnknownHostException e) {
            throw new BrokerException("Error allocating port for " + uri, e);
        }
    }

    public void releaseReceiver(URI uri) throws BrokerException {
        Receiver receiver = receivers.remove(uri.toString());
        if (receiver == null) {
            throw new BrokerException("Receiver not found for " + uri);
        }
        receiver.stop();
        String endpointId = uri.toString();
        allocator.release(endpointId);
    }

    public void send(byte[] message, int index, WorkContext context) {
        CallFrame frame = context.peekCallFrame();
        if (frame == null) {
            monitor.error("Callframe not found for callback");
            return;
        }
        String callback = frame.getCallbackUri();
        SenderHolder holder = senders.get(callback);
        if (holder == null) {
            holder = createSender(callback, true);
        }
        Sender sender = holder.getSender();
        if (sender instanceof OneWaySender) {
            ((OneWaySender) sender).send(message, index, context);
        } else {
            monitor.error("Callback sender is not a one-way type: " + holder.getClass().getName());
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

    private SenderHolder createSender(String endpointId, boolean oneWay) {
        ZMQ.Context context = manager.getContext();
        List<SocketAddress> addresses = addressCache.getActiveAddresses(endpointId);

        Sender sender;
        if (oneWay) {
            sender = new NonReliableOneWaySender(endpointId, context, addresses, pollTimeout, monitor);
        } else {
            sender = new NonReliableRequestReplySender(endpointId, context, addresses, pollTimeout, monitor);
        }


        SenderHolder holder = new SenderHolder(sender);
        sender.start();

        addressCache.subscribe(endpointId, sender);

        senders.put(endpointId, holder);
        return holder;
    }

    /**
     * Determines if the wire is one-way or request-reply. The first operation is used to determine if the contract is one-way as the binding does not
     * support mixing one-way and request-response operations on a service contract.
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

    private class SenderHolder {
        private Sender sender;
        private List<String> ids;

        private SenderHolder(Sender sender) {
            this.sender = sender;
            ids = new ArrayList<String>();
        }

        public Sender getSender() {
            return sender;
        }

        public List<String> getIds() {
            return ids;
        }
    }


}

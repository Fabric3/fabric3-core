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
package org.fabric3.binding.zeromq.runtime.message;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * Implements a basic SUB server with no qualities of service.
 * <p/>
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for connecting to one or more publishers and receiving
 * messages. The subscriber listens for address updates (e.g. a publisher coming online or going away). Since ZeroMQ does not implement disconnect semantics on
 * a socket, if an update is received the original socket will be closed and a new one created to connect to the update set of addresses.
 */
@Management
public class NonReliableSubscriber implements Subscriber, AddressListener {
    private static final byte[] EMPTY_BYTES = new byte[0];

    private String id;
    private String socketId = getClass().getName() + ":" + UUID.randomUUID();
    private ContextManager manager;
    private List<SocketAddress> addresses;
    private EventStreamHandler handler;
    private ZeroMQMetadata metadata;
    private ExecutorService executorService;

    private AtomicInteger connectionCount = new AtomicInteger();

    private SocketReceiver receiver;
    private long timeout;

    /**
     * Constructor
     *
     * @param id              the unique subscriber id, typically the consumer URI.
     * @param manager         the ZeroMQ context manager
     * @param addresses       the publisher addresses the subscriber must connect to
     * @param head            the head handler for dispatching events
     * @param metadata        subscriber metadata
     * @param executorService the executor for scheduling work
     */
    public NonReliableSubscriber(String id,
                                 ContextManager manager,
                                 List<SocketAddress> addresses,
                                 EventStreamHandler head,
                                 ZeroMQMetadata metadata,
                                 ExecutorService executorService) {
        this.id = id;
        this.manager = manager;
        this.addresses = addresses;
        this.handler = head;
        this.metadata = metadata;
        this.executorService = executorService;
        long specifiedTimeout = metadata.getTimeout();
        if (specifiedTimeout < 0) {
            this.timeout = specifiedTimeout;
        } else {
            this.timeout = TimeUnit.MILLISECONDS.toMicros(specifiedTimeout);
        }
    }

    @ManagementOperation(type = OperationType.POST)
    public void start() {
        if (receiver == null) {
            receiver = new SocketReceiver();
            executorService.submit(receiver);

        }
    }

    @ManagementOperation(type = OperationType.POST)
    public void stop() {
        try {
            receiver.stop();
        } finally {
            receiver = null;
        }
    }

    @ManagementOperation
    public List<String> getAddresses() {
        List<String> list = new ArrayList<String>();
        for (SocketAddress address : addresses) {
            list.add(address.toString());
        }
        return list;
    }

    public void incrementConnectionCount() {
        connectionCount.incrementAndGet();
    }

    public void decrementConnectionCount() {
        connectionCount.decrementAndGet();
    }

    public boolean hasConnections() {
        return connectionCount.get() > 0;
    }

    public String getId() {
        return id;
    }

    public void onUpdate(List<SocketAddress> addresses) {
        // refresh socket
        this.addresses = addresses;
        if (receiver != null) {
            receiver.refresh();
        }
    }

    /**
     * The message receiver. Responsible for creating socket connections to publishers and polling for messages.
     */
    class SocketReceiver implements Runnable {
        private Socket socket;
        private Socket controlSocket;

        private ZMQ.Poller poller;
        private AtomicBoolean active = new AtomicBoolean(true);
        private AtomicBoolean doRefresh = new AtomicBoolean(true);

        /**
         * Signals to closes the old socket and establish a new one when publisher addresses have changed in the domain.
         */
        public void refresh() {
            doRefresh.set(true);
        }

        /**
         * Stops polling and closes the existing socket.
         */
        public synchronized void stop() {
            active.set(false);
        }

        public void run() {
            try {
                while (active.get()) {
                    reconnect();
                    // Do not poll indefinitely since reconnect needs to be called periodically. Otherwise,  publisher socket address updates may not be
                    // received until after the poll returns (which may be never if all publisher addresses changed).
                    long val = poller.poll(timeout);
                    if (val > 0) {
                        // check if the message is a control message; if so, shutdown (currently the only implemented message)
                        byte[] controlPayload = controlSocket.recv(ZMQ.NOBLOCK);
                        if (controlPayload != null) {
                            closeSocket();
                            return;
                        }
                        byte[][] frames = null;
                        byte[] payload = socket.recv(0);
                        int index = 1;
                        while (socket.hasReceiveMore()) {
                            if (frames == null) {
                                frames = new byte[2][];
                                frames[0] = payload;
                            } else {
                                byte[][] newArray = new byte[frames.length + 1][];
                                System.arraycopy(frames, 0, newArray, 0, frames.length);
                                frames = newArray;
                            }
                            frames[index] = socket.recv(0);
                            index++;
                        }
                        if (frames == null) {
                            handler.handle(payload, true);
                        } else {
                            handler.handle(frames, true);
                        }
                    }
                }
                closeSocket();
            } catch (RuntimeException e) {
                // exception, make sure the thread is rescheduled
                executorService.submit(this);
                throw e;
            }
        }

        /**
         * Closes an existing socket and creates a new one, binding it to the list of active publisher endpoints.
         */
        private synchronized void reconnect() {
            if (!doRefresh.getAndSet(false)) {
                return;
            }
            closeSocket();
            manager.reserve(socketId);
            Context context = manager.getContext();
            socket = context.socket(ZMQ.SUB);
            SocketHelper.configure(socket, metadata);
            socket.subscribe(EMPTY_BYTES);    // receive all messages

            for (SocketAddress address : addresses) {
                socket.connect(address.toProtocolString());
            }

            // establish a socket for receiving control messages
            controlSocket = manager.createControlSocket();

            poller = context.poller();
            poller.register(controlSocket, ZMQ.Poller.POLLIN);
            poller.register(socket, ZMQ.Poller.POLLIN);
        }

        private void closeSocket() {
            if (socket != null) {
                try {
                    socket.close();
                } finally {
                    manager.release(socketId);
                }
            }
            if (controlSocket != null) {
                controlSocket.close();
            }
        }

    }

}

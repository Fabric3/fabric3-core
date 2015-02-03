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
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.federation.addressing.AddressListener;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * Implements a basic SUB server with no qualities of service.
 *
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
        List<String> list = new ArrayList<>();
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

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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.oasisopen.sca.ServiceRuntimeException;
import org.zeromq.ZMQ;

/**
 *
 */
@Management
public class NonReliableOneWaySender implements OneWaySender, Thread.UncaughtExceptionHandler {
    private static final Request SHUTDOWN = new Request(null, 0, null);
    private String id;
    private List<SocketAddress> addresses;
    private MessagingMonitor monitor;

    private SocketMultiplexer multiplexer;
    private Dispatcher dispatcher;

    private LinkedBlockingQueue<Request> queue;
    private long pollTimeout;

    public NonReliableOneWaySender(String id,
                                   ContextManager manager,
                                   List<SocketAddress> addresses,
                                   long pollTimeout,
                                   ZeroMQMetadata metadata,
                                   MessagingMonitor monitor) {
        this.id = id;
        this.addresses = addresses;
        this.pollTimeout = pollTimeout;
        this.monitor = monitor;
        queue = new LinkedBlockingQueue<>();
        multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.PUSH, metadata);
    }

    public void start() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            schedule();
        }
    }

    public void stop() {
        try {
            dispatcher.stop();
            queue.put(SHUTDOWN);
        } catch (InterruptedException e) {
            monitor.error(e);
        } finally {
            dispatcher = null;
        }
    }

    public String getId() {
        return id;
    }

    public void accept(EntryChange change, ServiceEntry entry) {
        // refresh socket
        this.addresses = AddressUpdater.accept(change, entry, addresses);
        dispatcher.refresh();
    }

    public void send(byte[] message, int index, WorkContext workContext) {
        try {
            Request request = new Request(message, index, serialize(workContext));
            queue.put(request);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ServiceRuntimeException(e);
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        // TODO use runtime thread pool
        Thread thread = new Thread(dispatcher);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }

    /**
     * Serializes the work context.
     *
     * @param workContext the work context
     * @return the serialized work context
     * @throws Fabric3Exception if a serialization error is encountered
     */
    private byte[] serialize(WorkContext workContext)  {
        List<String> stack = workContext.getCallbackReferences();
        if (stack == null || stack.isEmpty()) {
            return null;
        }

        return CallbackReferenceSerializer.serializeToBytes(stack);
    }

    /**
     * Dispatches requests to the ZeroMQ socket.
     */
    private class Dispatcher implements Runnable {
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
        public void stop() {
            active.set(false);
        }

        public void run() {
            while (active.get()) {
                try {
                    reconnect();

                    // handle pending requests
                    List<Request> drained = new ArrayList<>();
                    Request value = queue.poll(pollTimeout, TimeUnit.MICROSECONDS);
                    if (SHUTDOWN == value) {
                        multiplexer.close();
                        return;
                    }
                    // if no available socket, drop the message
                    if (!multiplexer.isAvailable()) {
                        monitor.dropMessage();
                        continue;
                    }

                    if (value != null) {
                        drained.add(value);
                        queue.drainTo(drained);
                    }
                    for (Request request : drained) {
                        if (SHUTDOWN == request) {
                            multiplexer.close();
                            return;
                        }
                        ZMQ.Socket socket = multiplexer.get();

                        // serialize the request payload
                        socket.send(request.getPayload(), ZMQ.SNDMORE);

                        // serialize the operation index
                        int index = request.getIndex();
                        byte[] context = request.getWorkContext();

                        byte[] serializedIndex = ByteBuffer.allocate(4).putInt(index).array();

                        if (context != null && context.length > 0) {
                            socket.send(serializedIndex, ZMQ.SNDMORE);
                            socket.send(context, 0);
                        } else {
                            socket.send(serializedIndex, 0);
                        }
                    }
                } catch (RuntimeException e) {
                    // exception, make sure the thread is rescheduled
                    schedule();
                    throw e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            multiplexer.close();
        }

        /**
         * Updates the multiplexer with new endpoint addresses.
         */
        private void reconnect() {
            if (!doRefresh.getAndSet(false)) {
                return;
            }
            multiplexer.update(addresses);
        }
    }

    private static class Request {
        private byte[] payload;
        private byte[] workContext;
        private int index;

        public Request(byte[] payload, int index, byte[] workContext) {
            this.payload = payload;
            this.index = index;
            this.workContext = workContext;
        }

        public byte[] getPayload() {
            return payload;
        }

        public int getIndex() {
            return index;
        }

        public byte[] getWorkContext() {
            return workContext;
        }

    }

}

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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.ServiceUnavailableException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * A {@link RequestReplySender} that provides no qualities of service.
 *
 * Since ZeroMQ requires the creating socket thread to dispatch messages, a looping thread is used for sending messages. Messages are provided to the thread via
 * a queue.
 */
public class NonReliableRequestReplySender implements RequestReplySender, Thread.UncaughtExceptionHandler {
    private static final Callable<byte[]> CALLABLE = () -> null;
    private static final Request SHUTDOWN = new Request(null, 0, null);

    private String id;
    private ContextManager manager;
    private List<SocketAddress> addresses;
    private long pollTimeout;
    private MessagingMonitor monitor;

    private Dispatcher dispatcher;

    private RoundRobinSocketMultiplexer multiplexer;
    private Map<Socket, ZMQ.Poller> pollers;

    private LinkedBlockingQueue<Request> queue;

    public NonReliableRequestReplySender(String id,
                                         ContextManager manager,
                                         List<SocketAddress> addresses,
                                         long pollTimeout,
                                         ZeroMQMetadata metadata,
                                         MessagingMonitor monitor) {
        this.id = id;
        this.manager = manager;
        this.addresses = addresses;
        this.pollTimeout = pollTimeout;
        this.monitor = monitor;
        multiplexer = new RoundRobinSocketMultiplexer(manager, ZMQ.XREQ, metadata);
        queue = new LinkedBlockingQueue<>();
        pollers = new ConcurrentHashMap<>();
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

    public byte[] sendAndReply(byte[] message, int index, WorkContext workContext) {
        try {
            byte[] serializedWorkContext = serialize(workContext);
            Request request = new Request(message, index, serializedWorkContext);
            queue.put(request);
            return request.get(100000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ServiceRuntimeException(e);
        } catch (ExecutionException e) {
            throw new ServiceRuntimeException(e);
        } catch (TimeoutException | Fabric3Exception e) {
            throw new ServiceUnavailableException(e);
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
    private byte[] serialize(WorkContext workContext) {
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
        private Socket controlSocket;

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
                        controlSocket.close();
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
                        Socket socket = multiplexer.get();

                        socket.send(request.getPayload(), ZMQ.SNDMORE);

                        // serialize the operation index
                        int index = request.getIndex();
                        byte[] serializedIndex = ByteBuffer.allocate(4).putInt(index).array();

                        byte[] context = request.getWorkContext();
                        if (context != null && context.length > 0) {
                            socket.send(serializedIndex, ZMQ.SNDMORE);
                            socket.send(context, 0);
                        } else {
                            socket.send(serializedIndex, 0);
                        }

                        ZMQ.Poller poller = pollers.get(socket);
                        long val = poller.poll(pollTimeout);
                        if (val < 0) {
                            // response timed out, return an error to the waiting thread
                            //noinspection ThrowableInstanceNeverThrown
                            request.setException(new ServiceUnavailableException("Timeout waiting on response"));
                            request.run();
                            continue;
                        }
                        byte[] controlPayload = controlSocket.recv(ZMQ.NOBLOCK);
                        if (controlPayload != null) {
                            multiplexer.close();
                            if (controlSocket != null) {
                                controlSocket.close();
                            }
                            return;
                        }

                        byte[] response = socket.recv(0);
                        request.set(response);
                        request.run();
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
            if (controlSocket != null) {
                controlSocket.close();
            }

        }

        /**
         * Closes an existing socket and creates a new one, binding it to the list of active service endpoints.
         */
        private void reconnect() {
            if (!doRefresh.getAndSet(false)) {
                return;
            }
            if (controlSocket == null) {
                controlSocket = manager.createControlSocket();
            }

            multiplexer.update(addresses);
            Collection<Socket> sockets = multiplexer.getAll();
            pollers.clear();
            for (Socket socket : sockets) {
                ZMQ.Poller poller = manager.getContext().poller();
                poller.register(socket, ZMQ.Poller.POLLIN);
                poller.register(controlSocket, ZMQ.Poller.POLLIN);
                pollers.put(socket, poller);
            }
        }
    }

    /**
     * A {@link Future} used to pass a request payload to the ZeroMQ socket thread and retrieve the invocation return value on completion.
     */
    private static class Request extends FutureTask<byte[]> {
        private byte[] payload;
        private byte[] workContext;
        private int index;

        public Request(byte[] payload, int index, byte[] workContext) {
            super(CALLABLE);
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

        @Override
        public void set(byte[] s) {
            super.set(s);
        }

        @Override
        protected void setException(Throwable t) {
            super.setException(t);
        }
    }

}

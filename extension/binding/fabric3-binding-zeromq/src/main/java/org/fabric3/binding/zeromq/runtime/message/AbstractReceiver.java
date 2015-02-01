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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.oasisopen.sca.ServiceRuntimeException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 *
 */
public abstract class AbstractReceiver implements Receiver, Thread.UncaughtExceptionHandler {

    protected ContextManager manager;
    protected SocketAddress address;
    protected ExecutorService executorService;

    protected int socketType;

    protected Interceptor[] interceptors;
    protected MessagingMonitor monitor;

    protected Receiver receiver;
    protected ZeroMQMetadata metadata;
    protected String id = getClass().getName() + ":" + UUID.randomUUID().toString();

    /**
     * Constructor.
     *
     * @param manager         the ZeroMQ Context manager
     * @param address         the address to receive messages on
     * @param chains          the invocation chains for dispatching invocations
     * @param socketType      the socket type as defined by ZeroMQ
     * @param metadata        metadata
     * @param executorService the executor for scheduling work
     * @param monitor         the monitor
     */
    public AbstractReceiver(ContextManager manager,
                            SocketAddress address,
                            List<InvocationChain> chains,
                            int socketType,
                            ZeroMQMetadata metadata,
                            ExecutorService executorService,
                            MessagingMonitor monitor) {
        this.manager = manager;
        this.address = address;
        this.executorService = executorService;
        this.interceptors = new Interceptor[chains.size()];
        for (int i = 0, chainsSize = chains.size(); i < chainsSize; i++) {
            InvocationChain chain = chains.get(i);
            interceptors[i] = chain.getHeadInterceptor();
        }
        this.socketType = socketType;
        this.metadata = metadata;
        this.monitor = monitor;
    }

    public void start() {
        if (receiver == null) {
            receiver = new Receiver();
            schedule();
        }
    }

    public void stop() {
        try {
            receiver.stop();
        } finally {
            receiver = null;
        }
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        executorService.submit(receiver);
    }

    /**
     * Creates a WorkContext for the request.
     * <p/>
     * client that is wired to it. Otherwise, it is null.
     *
     * @param header the serialized work context header
     * @return the work context
     */
    @SuppressWarnings({"unchecked"})
    protected WorkContext setWorkContext(byte[] header) {
        try {
            WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
            if (header == null || header.length == 0) {
                return workContext;
            }

            List<String> stack = CallbackReferenceSerializer.deserialize(header);
            // add the last callback twice as it will be needed when the callback is made back through the binding
            if (!stack.isEmpty()) {
                stack.add(stack.get(stack.size() - 1));
            }
            workContext.addCallbackReferences(stack);
            return workContext;
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException("Error deserializing callback references", e);
        }
    }

    protected abstract boolean invoke(Socket socket);

    protected abstract void response(Socket socket);

    /**
     * The message receiver. Responsible for creating socket connections to publishers and polling for messages.
     */
    private class Receiver implements Runnable {
        private Socket socket;
        private Socket controlSocket;

        private ZMQ.Poller poller;
        private AtomicBoolean active = new AtomicBoolean(true);

        /**
         * Signals to stops polling and close the receiver socket, if one is open. Note that the socket cannot be closed in this method, as it will be called on
         * a different thread than {@link #run()}, which opened the socket. ZeroMQ requires a socket only be accessed by the thread that created it.
         */
        public synchronized void stop() {
            active.set(false);
        }

        public void run() {
            try {
                bind();
                while (active.get()) {
                    if (poller == null) {
                        // the socket or poller could not be created, abort
                        monitor.error("Failed to initialize ZeroMQ socket, aborting receiver");
                        return;
                    }
                    long val = poller.poll();
                    if (val > 0) {
                        byte[] controlPayload = controlSocket.recv(ZMQ.NOBLOCK);
                        if (controlPayload != null) {
                            try {
                                socket.close();
                                controlSocket.close();
                            } finally {
                                manager.release(id);
                            }
                            return;
                        }

                        if (!invoke(socket)) {
                            continue;
                        }
                        response(socket);
                    }
                }
                // the socket must be closed here on this thread!
                if (socket != null) {
                    try {
                        socket.close();
                        controlSocket.close();
                    } finally {
                        manager.release(id);
                    }
                    socket = null;
                }
            } catch (RuntimeException e) {
                // exception, make sure the thread is rescheduled
                manager.release(id);
                schedule();
                throw e;
            }

        }

        private void bind() {
            if (socket != null) {
                // Socket is still active, ignore. This can happen if bind is called after the receiver has been rescheduled
                return;
            }
            manager.reserve(id);
            socket = manager.getContext().socket(socketType);
            SocketHelper.configure(socket, metadata);
            address.getPort().bind(Port.TYPE.TCP);
            socket.bind(address.toProtocolString());

            controlSocket = manager.createControlSocket();

            poller = manager.getContext().poller();
            poller.register(controlSocket, ZMQ.Poller.POLLIN);
            poller.register(socket, ZMQ.Poller.POLLIN);
        }

    }

}

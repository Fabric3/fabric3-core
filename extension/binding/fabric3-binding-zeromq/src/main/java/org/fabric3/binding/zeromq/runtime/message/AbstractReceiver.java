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

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.invocation.CallbackReference;
import org.fabric3.spi.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
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

            List<CallbackReference> stack = CallbackReferenceSerializer.deserialize(header);
            // add the last callback twice as it will be needed when the callback is made back through the binding
            if (!stack.isEmpty()) {
                stack.add(stack.get(stack.size() - 1));
            }
            workContext.addCallbackReferences(stack);
            return workContext;
        } catch (IOException e) {
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

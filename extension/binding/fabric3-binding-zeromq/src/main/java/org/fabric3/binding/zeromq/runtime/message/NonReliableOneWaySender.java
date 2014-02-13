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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.invocation.CallbackReference;
import org.fabric3.spi.container.invocation.CallbackReferenceSerializer;
import org.fabric3.spi.container.invocation.WorkContext;
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

    public void onUpdate(List<SocketAddress> addresses) {
        // refresh socket
        this.addresses = addresses;
        dispatcher.refresh();
    }

    public void send(byte[] message, int index, WorkContext workContext) {
        try {
            Request request = new Request(message, index, serialize(workContext));
            queue.put(request);
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new ServiceRuntimeException(e);
        } catch (IOException e) {
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
     * @throws IOException if a serialization error is encountered
     */
    private byte[] serialize(WorkContext workContext) throws IOException {
        List<CallbackReference> stack = workContext.getCallbackReferences();
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

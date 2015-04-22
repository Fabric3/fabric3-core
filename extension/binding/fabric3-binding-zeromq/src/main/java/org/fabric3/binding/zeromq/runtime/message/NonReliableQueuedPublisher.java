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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.spi.host.Port;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * Implements a basic PUB client with no qualities of service.
 *
 * Since ZeroMQ requires the creating socket thread to dispatch messages, a looping thread is used for publishing messages. Messages are provided to the thread
 * via a queue.
 */
@Management
public class NonReliableQueuedPublisher implements Publisher, Thread.UncaughtExceptionHandler {
    private static final byte[] SHUTDOWN = new byte[0];
    private ContextManager manager;
    private SocketAddress address;
    private long pollTimeout;
    private ZeroMQMetadata metadata;
    private MessagingMonitor monitor;

    private Socket socket;
    private Dispatcher dispatcher;

    private LinkedBlockingQueue<Object> queue;

    public NonReliableQueuedPublisher(ContextManager manager, SocketAddress address, ZeroMQMetadata metadata, long pollTimeout, MessagingMonitor monitor) {
        this.manager = manager;
        this.address = address;
        this.pollTimeout = pollTimeout;
        this.metadata = metadata;
        this.monitor = monitor;
        this.queue = new LinkedBlockingQueue<>();
    }

    @ManagementOperation(type = OperationType.POST)
    public void start() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
            schedule();
        }
    }

    @ManagementOperation(type = OperationType.POST)
    public void stop() {
        try {
            if (dispatcher != null) {
                dispatcher.stop();
            }
            queue.put(SHUTDOWN);
        } catch (InterruptedException e) {
            monitor.error(e);
        } finally {
            dispatcher = null;
        }
    }

    @ManagementOperation
    public String getAddress() {
        return address.toString();
    }

    public void publish(byte[] message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public void publish(byte[][] message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public void uncaughtException(Thread t, Throwable e) {
        monitor.error(e);
    }

    private void schedule() {
        Thread thread = new Thread(dispatcher);
        thread.setUncaughtExceptionHandler(this);
        thread.start();
    }

    private class Dispatcher implements Runnable {
        private AtomicBoolean active = new AtomicBoolean(true);

        public void stop() {
            active.set(false);
        }

        public void run() {
            String id = getClass().getName() + ":" + UUID.randomUUID().toString();
            manager.reserve(id);
            socket = manager.getContext().socket(ZMQ.PUB);
            SocketHelper.configure(socket, metadata);
            address.getPort().bind(Port.TYPE.TCP);
            socket.bind(address.toProtocolString());

            while (active.get()) {
                try {

                    Object value = queue.poll(pollTimeout, TimeUnit.MILLISECONDS);
                    if (SHUTDOWN == value) {
                        close(id);
                        return;
                    }
                    if (value == null) {
                        continue;
                    }
                    List<Object> drained = new ArrayList<>();
                    drained.add(value);
                    queue.drainTo(drained);
                    for (Object object : drained) {
                        if (object instanceof byte[]) {
                            // single frame message
                            socket.send((byte[]) object, 0);
                        } else if (object instanceof byte[][]) {
                            // multi-frame message - send each frame
                            byte[][] byteArray = (byte[][]) object;
                            int length = byteArray.length;
                            for (int i = 0; i < length - 1; i++) {
                                byte[] bytes = byteArray[i];
                                socket.send(bytes, ZMQ.SNDMORE);
                            }
                            socket.send(byteArray[length - 1], 0);
                        } else {
                            // programming error
                            monitor.error("Unknown object type:" + object.getClass().getName());
                        }
                    }
                } catch (RuntimeException e) {
                    // exception, make sure the thread is rescheduled
                    manager.release(id);
                    schedule();
                    throw e;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
            close(id);
        }

        private void close(String id) {
            if (socket != null) {
                try {
                    socket.close();
                } finally {
                    manager.release(id);
                }
            }
        }
    }

}

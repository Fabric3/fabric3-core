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
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.MessagingMonitor;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.zeromq.ZMQ;

/**
 * Implementation of a {@link Receiver} the implements one-way with no qualities of service.
 *
 * Since ZeroMQ requires the creating socket thread to receive messages, a polling thread is used for reading messages from the ZeroMQ socket. The receiver
 * listens for address updates (e.g. a sender coming online or going away). Since ZeroMQ does not implement disconnect semantics on a socket, if an update is
 * received the original socket will be closed and a new one created to connect to the updated set of addresses.
 */
@Management
public class NonReliableOneWayReceiver extends AbstractReceiver implements Thread.UncaughtExceptionHandler {

    /**
     * Constructor.
     *
     * @param manager         the ZeroMQ Context manager
     * @param address         the address to receive messages on
     * @param chains          the invocation chains for dispatching invocations
     * @param executorService the runtime executor service
     * @param metadata        metadata
     * @param monitor         the monitor
     */
    public NonReliableOneWayReceiver(ContextManager manager,
                                     SocketAddress address,
                                     List<InvocationChain> chains,
                                     ExecutorService executorService,
                                     ZeroMQMetadata metadata,
                                     MessagingMonitor monitor) {
        super(manager, address, chains, ZMQ.PULL, metadata, executorService, monitor);
    }

    @Override
    protected boolean invoke(ZMQ.Socket socket) {
        final byte[][] frames = new byte[3][];
        int i = 1;
        frames[0] = socket.recv(0);
        while (socket.hasReceiveMore()) {
            if (i > 2) {
                monitor.error("Invalid message: received more than three frames");
                return false;
            }
            frames[i] = socket.recv(0);
            i++;
        }
        executorService.submit(new Runnable() {
            public void run() {
                Message request = MessageCache.getAndResetMessage();
                try {
                    request.setBody(frames[0]);
                    int methodIndex = ByteBuffer.wrap(frames[1]).getInt();
                    WorkContext context = setWorkContext(frames[2]);

                    request.setWorkContext(context);

                    Interceptor interceptor = interceptors[methodIndex];

                    interceptor.invoke(request);
                } finally {
                    request.reset();
                }
            }
        });
        return true;
    }

    protected void response(ZMQ.Socket socket) {
        // no-op
    }
}



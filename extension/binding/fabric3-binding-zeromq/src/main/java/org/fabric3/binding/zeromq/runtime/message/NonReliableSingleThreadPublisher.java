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

import java.util.UUID;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * Implements a basic PUB client with no qualities of service.
 * <p/>
 * This implementation dispatches to the ZeroMQ socket on the same thread that invoked it. Since ZeroMQ requires a socket to only be invoked by the thread that
 * created it, this implementation can only be used with a single worker thread that receives from a channel (e.g. a ring buffer setup).
 */
@Management
public class NonReliableSingleThreadPublisher implements Publisher {
    private ContextManager manager;
    private SocketAddress address;
    private ZeroMQMetadata metadata;

    private Socket socket;
    private final String id;

    public NonReliableSingleThreadPublisher(ContextManager manager, SocketAddress address, ZeroMQMetadata metadata) {
        this.manager = manager;
        this.address = address;
        this.metadata = metadata;
        id = getClass().getName() + ":" + UUID.randomUUID().toString();
    }

    @ManagementOperation(type = OperationType.POST)
    public void start() {
        manager.reserve(id);
        socket = manager.getContext().socket(ZMQ.PUB);
        SocketHelper.configure(socket, metadata);
        address.getPort().bind(Port.TYPE.TCP);
        socket.bind(address.toProtocolString());
    }

    @ManagementOperation(type = OperationType.POST)
    public void stop() {
        if (socket != null) {
            try {
                socket.close();
            } finally {
                socket = null;
                manager.release(id);
            }
        }
    }

    @ManagementOperation
    public String getAddress() {
        return address.toString();
    }

    public void publish(byte[] message) {
        if (socket == null) {
            return;
        }
        // single frame message
        socket.send(message, 0);
    }

    public void publish(byte[][] message) {
        if (socket == null) {
            return;
        }
        int length = message.length;
        for (int i = 0; i < length - 1; i++) {
            byte[] bytes = message[i];
            socket.send(bytes, ZMQ.SNDMORE);
        }
        socket.send(message[length - 1], 0);

    }

}

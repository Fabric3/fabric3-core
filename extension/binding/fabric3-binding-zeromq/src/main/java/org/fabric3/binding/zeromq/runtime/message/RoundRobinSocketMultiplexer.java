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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.spi.host.Port;
import org.zeromq.ZMQ;

/**
 * Implements a round-robin strategy for selecting an available socket from a collection of sockets.
 *
 * Note: Due to restrictions imposed by ZeroMQ, an instance of this class must be called on the same thread at all times.
 */
public class RoundRobinSocketMultiplexer implements SocketMultiplexer {
    private ContextManager manager;
    private int socketType;
    private ZeroMQMetadata metadata;

    private int index;
    private ZMQ.Socket[] zmqSockets;
    private Map<SocketAddress, ZMQ.Socket> sockets;

    private String seed = UUID.randomUUID().toString();

    public RoundRobinSocketMultiplexer(ContextManager manager, int socketType, ZeroMQMetadata metadata) {
        this.manager = manager;
        this.socketType = socketType;
        this.metadata = metadata;
        sockets = new HashMap<>();
    }

    public void update(List<SocketAddress> addresses) {
        if (sockets.isEmpty()) {
            if (addresses.size() == 1) {
                SocketAddress address = addresses.get(0);
                String addressString = address.toProtocolString();
                manager.reserve(getClass().getName() + ":" + seed + addressString);
                ZMQ.Socket socket = manager.getContext().socket(socketType);
                SocketHelper.configure(socket, metadata);
                address.getPort().bind(Port.TYPE.TCP);
                socket.connect(addressString);
                sockets.put(address, socket);
            } else {
                for (SocketAddress address : addresses) {
                    String addressString = address.toProtocolString();
                    manager.reserve(getClass().getName() + ":" + seed + addressString);
                    ZMQ.Socket socket = manager.getContext().socket(socketType);
                    SocketHelper.configure(socket, metadata);
                    address.getPort().bind(Port.TYPE.TCP);
                    socket.connect(addressString);
                    sockets.put(address, socket);
                }
            }
        } else {
            Set<SocketAddress> intersection = new HashSet<>(addresses);
            intersection.retainAll(sockets.keySet());

            Set<SocketAddress> toClose = new HashSet<>(sockets.keySet());
            toClose.removeAll(addresses);

            Set<SocketAddress> toAdd = new HashSet<>(addresses);
            toAdd.removeAll(sockets.keySet());

            try {
                for (SocketAddress address : toClose) {
                    sockets.remove(address).close();
                }
            } finally {
                for (SocketAddress address : toClose) {
                    manager.release(getClass().getName() + ":" + seed + address.toProtocolString());
                }
            }

            for (SocketAddress address : toAdd) {
                String addressString = address.toProtocolString();
                manager.reserve(getClass().getName() + ":" + seed + addressString);
                ZMQ.Socket socket = manager.getContext().socket(socketType);
                SocketHelper.configure(socket, metadata);
                address.getPort().bind(Port.TYPE.TCP);
                socket.connect(addressString);
                sockets.put(address, socket);
            }

        }
        zmqSockets = new ZMQ.Socket[sockets.size()];
        sockets.values().toArray(zmqSockets);
    }

    public ZMQ.Socket get() {
        if (index == zmqSockets.length) {
            index = 0;
        }
        return zmqSockets[index++];
    }

    public Collection<ZMQ.Socket> getAll() {
        return sockets.values();
    }

    public boolean isAvailable() {
        return zmqSockets.length > 0;
    }

    public void close() {
        try {
            for (ZMQ.Socket socket : sockets.values()) {
                socket.close();
            }
        } finally {
            for (SocketAddress address : sockets.keySet()) {
                manager.release(getClass().getName() + ":" + seed + address.toProtocolString());
            }

        }
    }


}

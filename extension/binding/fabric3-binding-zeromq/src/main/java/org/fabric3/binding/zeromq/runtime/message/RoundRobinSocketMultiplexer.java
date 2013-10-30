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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
import org.fabric3.spi.host.Port;
import org.zeromq.ZMQ;

/**
 * Implements a round-robin strategy for selecting an available socket from a collection of sockets.
 * <p/>
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
        sockets = new HashMap<SocketAddress, ZMQ.Socket>();
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
            Set<SocketAddress> intersection = new HashSet<SocketAddress>(addresses);
            intersection.retainAll(sockets.keySet());

            Set<SocketAddress> toClose = new HashSet<SocketAddress>(sockets.keySet());
            toClose.removeAll(addresses);

            Set<SocketAddress> toAdd = new HashSet<SocketAddress>(addresses);
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

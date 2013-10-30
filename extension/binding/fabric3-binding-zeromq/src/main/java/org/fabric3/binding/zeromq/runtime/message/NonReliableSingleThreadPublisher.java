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

import java.util.UUID;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.federation.addressing.SocketAddress;
import org.fabric3.binding.zeromq.runtime.context.ContextManager;
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
        // single frame message
        socket.send(message, 0);
    }

    public void publish(byte[][] message) {
        int length = message.length;
        for (int i = 0; i < length - 1; i++) {
            byte[] bytes = message[i];
            socket.send(bytes, ZMQ.SNDMORE);
        }
        socket.send(message[length - 1], 0);

    }

}

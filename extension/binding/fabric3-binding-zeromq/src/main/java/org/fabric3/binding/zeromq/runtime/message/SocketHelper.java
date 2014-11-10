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

import org.zeromq.ZMQ;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;

/**
 * Helper methods for configuring ZeroMQ sockets.
 */
public final class SocketHelper {

    private SocketHelper() {
    }

    /**
     * Configures a socket based on metadata.
     *
     * @param socket   the socket
     * @param metadata the metadata
     */
    public static void configure(ZMQ.Socket socket, ZeroMQMetadata metadata) {
        socket.setLinger(0);
        if (metadata.getHighWater() > -1) {
            socket.setHWM(metadata.getHighWater());
        } else {
            socket.setHWM(1000);
        }
        if (metadata.getMulticastRate() > -1) {
            socket.setRate(metadata.getMulticastRate());
        }
        if (metadata.getMulticastRecovery() > -1) {
            socket.setRecoveryInterval(metadata.getMulticastRecovery());
        }
        if (metadata.getReceiveBuffer() > -1) {
            socket.setReceiveBufferSize(metadata.getReceiveBuffer());
        }
        if (metadata.getSendBuffer() > -1) {
            socket.setSendBufferSize(metadata.getSendBuffer());
        }
    }
}

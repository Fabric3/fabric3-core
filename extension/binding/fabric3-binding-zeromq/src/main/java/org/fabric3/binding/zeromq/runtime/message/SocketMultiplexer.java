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
import java.util.List;

import org.fabric3.spi.federation.addressing.SocketAddress;
import org.zeromq.ZMQ;

/**
 * Implementations return an available socket from a collection based on a selection algorithm such as round-robin.
 * <p/>
 * Note multiplexers are not thread safe and are designed to be called from a single thread.
 */
public interface SocketMultiplexer {

    /**
     * Replaces the previous list of available sockets with a new one.
     *
     * @param addresses the new socket addresses
     */
    void update(List<SocketAddress> addresses);

    /**
     * Returns the next available socket.
     *
     * @return the next available socket
     */
    ZMQ.Socket get();

    /**
     * Returns all active sockets.
     *
     * @return all active sockets
     */
    Collection<ZMQ.Socket> getAll();

    /**
     * Returns true if the multiplexer has an available socket.
     *
     * @return true if the multiplexer has an available socket
     */
    boolean isAvailable();

    /**
     * Closes the underlying collection of sockets.
     */
    void close();
}

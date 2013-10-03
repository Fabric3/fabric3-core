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
import java.util.List;

import org.zeromq.ZMQ;

import org.fabric3.spi.federation.addressing.SocketAddress;

/**
 * Implementations return an available socket from a collection based on a selection algorithm such as round-robin.
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

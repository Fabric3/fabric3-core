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
package org.fabric3.binding.zeromq.runtime.context;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

/**
 * Manages the ZeroMQ Context lifecycle.
 * <p/>
 * Note when sockets are created from the managed <code>Context</code>, clients must reserve a lease using {@link #reserve(String)}. When a socket is closed,
 * clients must call {@link #release(String)}.
 */
public interface ContextManager {

    /**
     * Returns the active ZeroMQ context.
     *
     * @return the active ZeroMQ context
     */
    Context getContext();

    /**
     * Creates and returns a connected socket for receiving control messages such as shutdown. Clients are responsible for closing the socket when finished.
     *
     * @return the control socket
     */
    ZMQ.Socket createControlSocket();

    /**
     * Reserves a socket lease. The context manager will not close the active ZeroMQ context on runtime shutdown until all leases have been released.
     *
     * @param id the unique client id
     */
    void reserve(String id);

    /**
     * Releases a socket lease.
     *
     * @param id the unique client id used to obtain the lease.
     */
    void release(String id);

}

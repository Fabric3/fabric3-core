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

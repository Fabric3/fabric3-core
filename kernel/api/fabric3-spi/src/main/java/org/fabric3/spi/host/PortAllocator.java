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
package org.fabric3.spi.host;

import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Responsible for allocating ports for use by a binding transport or other extension.
 * <p/>
 * A runtime can be configured with a range of ports, which serves as a pool for transports to obtain an open port using {@link #allocate(String,
 * String)}. If the runtime is not configured with a port range, services requiring sockets should use #{link #reserve} to reserve a port.
 */
public interface PortAllocator {

    int NOT_ALLOCATED = -1;

    /**
     * True if the allocator is configured to pool.
     *
     * @return true if the allocator is configured to pool
     */
    boolean isPoolEnabled();

    /**
     * Allocates a port for the given transport.
     *
     * @param name the port name. Used when a transport uses a port per endpoint
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     * @return the allocated port
     * @throws Fabric3Exception if there was an error allocating a port
     */
    Port allocate(String name, String type) throws Fabric3Exception;

    /**
     * Requests a specific port number to be reserved. This may be outside the configured port range. If so, the allocator will check availability and
     * track the port as being allocated if available.
     *
     * @param name the port name. Used when a transport uses a port per endpoint
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     * @param port the requested port number
     * @return returns the port
     * @throws Fabric3Exception if there was an error reserving the port
     */
    Port reserve(String name, String type, int port) throws Fabric3Exception;

    /**
     * Returns the port number associated with the name.
     *
     * @param name the port name
     * @return the allocated port number or {@link #NOT_ALLOCATED} if a port has not been allocated
     */
    int getAllocatedPortNumber(String name);

    /**
     * Returns a list of allocated port types.
     *
     * @return a list of allocated port types
     */
    Set<String> getPortTypes();

    /**
     * Releases the given port and makes it available for re-allocation.
     *
     * @param port the port
     */
    void release(int port);

    /**
     * Releases a port for the given transport and makes it available for re-allocation.
     *
     * @param name the port name
     */
    void release(String name);

    /**
     * Releases a port for the given transport and makes it available for re-allocation.
     *
     * @param type the transport type
     */
    void releaseAll(String type);

}

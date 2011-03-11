/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.host;

/**
 * Responsible for allocating ports for use by a binding transport or other extension. A runtime can be configured with a range of ports, which serves
 * as a pool for transports to obtain on open port using {@link #allocate(String)}. If the runtime is not configured with a port range, services
 * requiring sockets should use #{link #reserve} to reserve a port.
 *
 * @version $Rev$ $Date$
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
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     * @return the allocated port number
     * @throws PortAllocationException if there was an error allocating a port
     */
    int allocate(String type) throws PortAllocationException;

    /**
     * Requests a specific port number to be reserved. This may be outside the configured port range. If so, the allocator will check availability and
     * track the port as being allocated if available.
     *
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     * @param port the requested port number
     * @throws PortAllocationException if there was an error reserving the port
     */
    void reserve(String type, int port) throws PortAllocationException;

    /**
     * Returns the port number in use by the given transport.
     *
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     * @return the allocated port number or {@link #NOT_ALLOCATED} if a port has not been allocated
     */
    int getAllocatedPort(String type);

    /**
     * Releases a port for the given transport and makes it available for re-allocation.
     *
     * @param type the transport type, e.g. HTTP, HTTPS, FTP, TCP
     */
    void release(String type);

}

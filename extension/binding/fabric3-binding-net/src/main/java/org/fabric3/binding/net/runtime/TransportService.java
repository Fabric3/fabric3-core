/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
*/
package org.fabric3.binding.net.runtime;

import org.fabric3.spi.builder.WiringException;

/**
 * Registers wires for services with an transport channel.
 *
 * @version $Rev$ $Date$
 */
public interface TransportService {

    /**
     * Returns the HTTP port in use by the service.
     *
     * @return the HTTP port in use by the service
     */
    int getHttpPort();

    /**
     * Returns the HTTPs port in use by the service
     *
     * @return the HTTPs port in use by the service
     */
    int getHttpsPort();

    /**
     * Returns the TCP port in use by the service
     *
     * @return the TCP port in use by the service
     */
    int getTcpPort();

    /**
     * Register the wire with the HTTP channel.
     *
     * @param path       the service path which is its relative URI
     * @param wireHolder the WireHolder containing the Wire and WireFormatter for dispatching to the service at the path
     * @throws WiringException if an exception registering the wire occurs
     */
    void registerHttp(String path, WireHolder wireHolder) throws WiringException;

    /**
     * Register the wire with the default TCP channel.
     *
     * @param path       the service path which is its relative URI
     * @param wireHolder the WireHolder containing the Wire and WireFormatter for dispatching to the service at the path
     * @throws WiringException if an exception registering the wire occurs
     */
    void registerTcp(String path, WireHolder wireHolder) throws WiringException;

    /**
     * Unregisters a service bound over HTTP.
     *
     * @param path the service path
     */
    void unregisterHttp(String path);

    /**
     * Unregisters a service bound over TCP.
     *
     * @param path the service path
     */
    void unregisterTcp(String path);
}

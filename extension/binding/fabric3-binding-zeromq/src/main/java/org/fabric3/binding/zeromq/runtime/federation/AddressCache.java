/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.zeromq.runtime.federation;

import java.util.List;

import org.fabric3.binding.zeromq.runtime.SocketAddress;

/**
 * Provides a view on the state of all ZeroMQ endpoints in the domain. An endpoint is the socket which binds to an address and can either be the
 * ZeroMQ client or server side of a connection.
 *
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public interface AddressCache {

    /**
     * Returns a collections of active socket addresses for the endpoint in the domain.
     *
     * @param endpointId the endpoint id.
     * @return a collections of active socket addresses for the endpoint in the domain
     */
    List<SocketAddress> getActiveAddresses(String endpointId);

    /**
     * Publishes an address event.
     *
     * @param event the address event
     */
    void publish(AddressEvent event);

    /**
     * Subscribes a listener to receive notifications when a socket associated with the endpoint changes in the domain.
     *
     * @param endpointId the endpoint id
     * @param listener   the listener
     */
    void subscribe(String endpointId, AddressListener listener);

    /**
     * Unsubscribes a listener.
     *
     * @param endpointId the endpoint id
     * @param listenerId the listener id
     */
    void unsubscribe(String endpointId, String listenerId);

}

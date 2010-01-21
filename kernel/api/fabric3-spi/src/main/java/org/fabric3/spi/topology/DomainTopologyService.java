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
package org.fabric3.spi.topology;

import java.util.List;

/**
 * Responsible for group communications across a domain.
 *
 * @version $Rev$ $Date$
 */
public interface DomainTopologyService {

    /**
     * Returns the active zones in the domain.
     *
     * @return the active zones in the domain or an empty list if none are active
     */
    List<String> getZones();

    /**
     * Returns the active runtimes in the domain.
     *
     * @return the active runtimes in the domain or an empty list if none are active
     */
    List<RuntimeInstance> getRuntimes();

    /**
     * Returns transport information such as port numbers in effect for a zone.
     *
     * @param zone      the zone
     * @param transport the transport name
     * @return the transport information or null if not found
     */
    String getTransportMetaData(String zone, String transport);

    /**
     * Sends a message asynchronously to all runtimes in the domain.
     *
     * @param payload the message payload
     * @throws MessageException if there is an error sending the message
     */
    void broadcastMessage(byte[] payload) throws MessageException;

    /**
     * Sends a message asynchronously to all runtimes in a zoone.
     *
     * @param zoneName the zone
     * @param payload  the message payload
     * @throws MessageException if there is an error sending the message
     */
    void broadcastMessage(String zoneName, byte[] payload) throws MessageException;

    /**
     * Sends a message synchronously to all runtimes in a zone.
     *
     * @param zoneName the zone
     * @param payload  the message payload
     * @param timeout  the time to wait on a response
     * @return the response messages
     * @throws MessageException if there is an error sending the message
     */
    List<byte[]> sendSynchronousMessageToZone(String zoneName, byte[] payload, long timeout) throws MessageException;

    /**
     * Sends a message synchronously to a runtime.
     *
     * @param runtimeName the runtime
     * @param payload     the message payload
     * @param timeout     the time to wait on a response
     * @return the response messages
     * @throws MessageException if there is an error sending the message
     */
    byte[] sendSynchronousMessage(String runtimeName, byte[] payload, long timeout) throws MessageException;

}
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
 * Responsible for group communications within a domain zone. This service is present only on participant runtimes in a federated topology and
 * provides low-level communications between a participant and other runtimes (a participant or controller). Higher-level communications semantics
 * such as deployment can be layered over this service.
 *
 * @version $Rev$ $Date$
 */
public interface ZoneTopologyService {

    /**
     * Returns true if the current runtime is the zone leader.
     *
     * @return true if the current runtime is the zone leader
     */
    boolean isZoneLeader();

    /**
     * Returns true if the domain controller is available.
     *
     * @return true if the domain controller is available
     */
    boolean isControllerAvailable();

    /**
     * Returns the name of the zone leader.
     *
     * @return the name of the zone leader or null if the current runtime has not joined the domain
     */
    String getZoneLeader();

    /**
     * Sends a message asynchronously to all runtimes in the zone.
     *
     * @param message the serialized message
     * @throws MessageException if an error occurs sending the message
     */
    void broadcastMessage(byte[] message) throws MessageException;

    /**
     * Sends a message asynchronously to a runtime.
     *
     * @param runtimeName the runtime
     * @param message     the serialized message
     * @throws MessageException if an error occurs sending the message
     */
    void sendAsynchronousMessage(String runtimeName, byte[] message) throws MessageException;

    /**
     * Sends a message synchronously to a runtime.
     *
     * @param runtimeName the runtime
     * @param message     the serialized message
     * @param timeout     the time to wait on a response
     * @return the response
     * @throws MessageException if an error occurs sending the message
     */
    Response sendSynchronousMessage(String runtimeName, byte[] message, long timeout) throws MessageException;

    /**
     * Sends a message synchronously to all runtimes in the zone.
     *
     * @param message the serialized message
     * @param timeout the timout to wait for responses
     * @return the responses
     * @throws MessageException if an error occurs sending the message
     */
    List<Response> sendSynchronousMessage(byte[] message, long timeout) throws MessageException;

    /**
     * Sends a message synchronously to the controller.
     *
     * @param message the serialized message
     * @param timeout the time to wait on a response
     * @return the response
     * @throws MessageException if an error occurs sending the message. {@link ControllerNotFoundException} wil be thrown if a controller
     */
    Response sendSynchronousControllerMessage(byte[] message, long timeout) throws MessageException;

    /**
     * Sends a message asynchronously to the controller.
     *
     * @param message the serialized message
     * @throws MessageException if an error occurs sending the message
     */
    void sendAsynchronousControllerMessage(byte[] message) throws MessageException;

}

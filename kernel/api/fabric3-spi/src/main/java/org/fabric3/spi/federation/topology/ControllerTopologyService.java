/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.spi.federation.topology;

import java.util.List;
import java.util.Set;

import org.fabric3.spi.domain.command.Command;
import org.fabric3.spi.domain.command.Response;
import org.fabric3.spi.domain.command.ResponseCommand;

/**
 * Responsible for controller communications across a federated (distributed) domain.
 * <p/>
 * This service is present only on controller runtimes in a federated topology and provides low-level communications between a controller and participants.
 * Higher-level communications semantics such as deployment can be layered over this service.
 */
public interface ControllerTopologyService extends TopologyService {

    /**
     * Returns the active zones in the domain.
     *
     * @return the active zones in the domain or an empty list if none are active
     */
    Set<Zone> getZones();

    /**
     * Returns the active runtimes in the domain.
     *
     * @return the active runtimes in the domain or an empty list if none are active
     */
    List<RuntimeInstance> getRuntimes();

    /**
     * Sends a command asynchronously to all runtimes in the domain.
     *
     * @param command the command
     * @throws MessageException if there is an error sending the message
     */
    void broadcast(Command command) throws MessageException;

    /**
     * Sends a command asynchronously to all runtimes in a zone.
     *
     * @param zoneName the zone
     * @param command  the command
     * @throws MessageException if there is an error sending the message
     */
    void broadcast(String zoneName, Command command) throws MessageException;

    /**
     * Sends a command synchronously to all runtimes in a zone.
     *
     * @param zoneName the zone
     * @param command  the command
     * @param failFast determines if fail-fast behavior should be observed; i.e. if an error is received, the remaining synchronous calls will not be made.
     *                 Otherwise, all synchronous calls will be attempted.
     * @param timeout  the time to wait on a response
     * @return the response messages. If an error was encountered and fail-fast is enabled, the responses will include all successful ones made up to the point
     * the error was received. The error response will be the last in the list. If fail-fast is not enabled, responses from all runtimes in the zone will be
     * received, possibly included multiple error responses.
     * @throws MessageException if there is an error sending the message
     */
    List<Response> sendSynchronousToZone(String zoneName, ResponseCommand command, boolean failFast, long timeout) throws MessageException;

}
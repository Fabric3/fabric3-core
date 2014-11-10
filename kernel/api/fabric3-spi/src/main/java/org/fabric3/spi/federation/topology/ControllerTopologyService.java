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
 */
package org.fabric3.spi.federation.topology;

import java.util.List;
import java.util.Set;

import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.container.command.ResponseCommand;

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
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

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.container.command.ResponseCommand;

/**
 * Responsible for controller communications across a federated (distributed) domain consisting of node runtimes.
 */
public interface NodeTopologyService {

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

    /**
     * Returns true if the current runtime is the zone leader.
     *
     * @return true if the current runtime is the zone leader
     */
    boolean isZoneLeader();

    /**
     * Registers a transient {@link TopologyListener}.
     *
     * @param listener the listener
     */
    void register(TopologyListener listener);

    /**
     * De-registers a transient {@link TopologyListener}.
     *
     * @param listener the listener
     */
    void deregister(TopologyListener listener);

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
    String getZoneLeaderName();

    /**
     * Sends a command synchronously to a runtime.
     *
     * @param runtimeName the runtime
     * @param command     the command
     * @param timeout     the time to wait on a response
     * @return the response
     * @throws MessageException if an error occurs sending the message
     */
    Response sendSynchronous(String runtimeName, ResponseCommand command, long timeout) throws MessageException;

    /**
     * Sends a command synchronously to the controller.
     *
     * @param command the command
     * @param timeout the time to wait on a response
     * @return the response
     * @throws MessageException if an error occurs sending the message.
     */
    Response sendSynchronousToController(ResponseCommand command, long timeout) throws MessageException;

    /**
     * Asynchronously sends a message over the given channel to the specified runtime.
     *
     * @param runtimeName the runtime
     * @param name        the channel name
     * @param message     the message
     * @throws MessageException if there is an error sending the message
     */
    void sendAsynchronous(String runtimeName, String name, Serializable message) throws MessageException;

    void closeChannel(String name) throws ZoneChannelException;

    void sendAsynchronous(String name, Serializable message) throws MessageException;

    void openChannel(String name, String configuration, MessageReceiver receiver, TopologyListener listener) throws ZoneChannelException;

}

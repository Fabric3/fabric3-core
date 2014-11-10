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

import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.container.command.ResponseCommand;

/**
 * Responsible for participant communications within a domain zone.
 * <p/>
 * This service is present only on participant runtimes in a federated topology and provides low-level communications between a participant and other runtimes
 * (a participant or controller). Higher-level communications semantics can be layered over this service.
 */
public interface ParticipantTopologyService extends TopologyService{

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
     * @throws MessageException if an error occurs sending the message. {@link ControllerNotFoundException} wil be thrown if a controller
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

}

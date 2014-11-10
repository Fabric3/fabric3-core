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

/**
 * Base federation interface for controller, participant and node runtimes.
 */
public interface TopologyService {

    /**
     * Returns true if the channel is open.
     *
     * @param name the channel name
     * @return true if the channel is open
     */
    boolean isChannelOpen(String name);

    /**
     * Opens a channel.
     *
     * @param name          the channel name
     * @param configuration the channel configuration or null to use the default configuration
     * @param receiver      the receiver to callback when a message is received
     * @param listener      an optional topology listener. May be null.
     * @throws ZoneChannelException if an error occurs opening the channel
     */
    void openChannel(String name, String configuration, MessageReceiver receiver, TopologyListener listener) throws ZoneChannelException;

    /**
     * Closes a channel.
     *
     * @param name the channel name
     * @throws ZoneChannelException if an error occurs closing the channel
     */
    void closeChannel(String name) throws ZoneChannelException;

    /**
     * Asynchronously sends a message over the given channel.
     *
     * @param name    the channel name
     * @param message the message
     * @throws MessageException if there is an error sending the message
     */
    void sendAsynchronous(String name, Serializable message) throws MessageException;

}

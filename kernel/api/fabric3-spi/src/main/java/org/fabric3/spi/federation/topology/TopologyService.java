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

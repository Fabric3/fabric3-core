/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.spi.channel;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.spi.model.physical.ChannelSide;

/**
 * Manages channels on a runtime.
 */
public interface ChannelManager {

    /**
     * Returns the channel or null if one does not exist.
     *
     * @param uri the channel URI
     * @param channelSide the channel side
     * @return the channel or null
     */
    Channel getChannel(URI uri, ChannelSide channelSide);

    /**
     * Returns the channel and increments its use count or null if one does not exist.
     *
     * @param uri the channel URI
     * @param channelSide the channel side
     * @return the channel or null
     */
    Channel getAndIncrementChannel(URI uri, ChannelSide channelSide);

    /**
     * Returns the channel and decrements its use count or null if one does not exist.
     *
     * @param uri the channel URI
     * @param channelSide the channel side
     * @return the channel or null
     */
    Channel getAndDecrementChannel(URI uri, ChannelSide channelSide);

    /**
     * Returns the use count for the channel or -1 if the channel is not registered.
     *
     * @param uri the channel uri
     * @param channelSide the channel side
     * @return the use count
     */
    int getCount(URI uri, ChannelSide channelSide);

    /**
     * Registers a channel.
     *
     * @param channel the channel
     * @throws RegistrationException if there is an error registering the channel
     */
    void register(Channel channel) throws RegistrationException;

    /**
     * Removes a channel for the given URI.
     *
     * @param uri         the uri
     * @param channelSide the channel side
     * @return the channel or null
     * @throws RegistrationException if there is an error removing the channel
     */
    Channel unregister(URI uri, ChannelSide channelSide) throws RegistrationException;

    /**
     * Starts channels contained in the given deployable composite.
     *
     * @param deployable the composite
     */
    void startContext(QName deployable);

    /**
     * Stops channels contained in the given deployable composite.
     *
     * @param deployable the composite
     */
    void stopContext(QName deployable);

}

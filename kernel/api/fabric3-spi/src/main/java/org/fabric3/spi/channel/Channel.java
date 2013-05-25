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

/**
 * An event channel. Responsible for transmitting events from producers to consumers.
 */
public interface Channel {
    /**
     * Returns the channel URI.
     *
     * @return the channel URI
     */
    URI getUri();

    /**
     * Returns the composite this channel was deployed with.
     *
     * @return the deployable composite
     */
    QName getDeployable();

    /**
     * Initializes the channel to receive events.
     */
    void start();

    /**
     * Stops the channel and prepares it for un-deployment
     */
    void stop();

    /**
     * Adds a handler for transmitting events to the channel.
     *
     * @param handler the handler
     */
    void addHandler(EventStreamHandler handler);

    /**
     * Removes a handler.
     *
     * @param handler the handler
     */
    void removeHandler(EventStreamHandler handler);

    /**
     * Attach a single handler to the channel so that it can flow events.
     *
     * @param handler the handler to attach
     */
    public void attach(EventStreamHandler handler);

    /**
     * Attach a connection to the channel so that it can flow events.
     *
     * @param connection the connection to attach
     */
    void attach(ChannelConnection connection);

    /**
     * Subscribe to receive events from the channel.
     *
     * @param uri        the URI identifying the subscription
     * @param connection the connection to receive events on
     */
    void subscribe(URI uri, ChannelConnection connection);

    /**
     * Unsubscribe from receiving events from the channel
     *
     * @param uri the subscription URI
     * @return the unsubscribed connection
     */
    ChannelConnection unsubscribe(URI uri);

}
/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime;

import java.net.URI;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.spi.container.channel.ChannelConnection;

/**
 * Responsible for managing local publishers and subscribers. Unlike brokers in traditional hub-and-spoke messaging architectures, implementations do not
 * receive or forward messages; rather, subscribers connect directly to publishers.
 */
public interface ZeroMQPubSubBroker {

    /**
     * Subscribes a consumer to the given channel.
     *
     * @param subscriberId the unique subscription id
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @param connection   the consumer connection to dispatch received message to
     * @param loader       the classloader for deserializing events, typically the consumer implementation contribution classloader
     * @throws BrokerException if an error occurs creating the subscription
     */
    void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) throws BrokerException;

    /**
     * Removes a consumer from the given channel.
     *
     * @param subscriberId the unique subscription id
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @throws BrokerException if an error occurs removing the subscription
     */
    void unsubscribe(URI subscriberId, ZeroMQMetadata metadata) throws BrokerException;

    /**
     * Connects the channel connection to the publisher for the given channel.
     *
     * @param connectionId the unique id for the connection
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @param connection   the producer that dispatches messages to the publisher
     * @param loader       the classloader for the event types being sent
     * @throws BrokerException if an error occurs removing the subscription
     */
    void connect(String connectionId, ZeroMQMetadata metadata, boolean dedicatedThread, ChannelConnection connection, ClassLoader loader)
            throws BrokerException;

    /**
     * Releases a publisher for a channel.
     *
     * @param connectionId the unique id for the connection
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @throws BrokerException if an error occurs removing the publisher
     */
    void release(String connectionId, ZeroMQMetadata metadata) throws BrokerException;

    /**
     * Starts all publishers and subscribers.
     */
    void startAll();

    /**
     * Stops all publishers and subscribers.
     */
    void stopAll();

}

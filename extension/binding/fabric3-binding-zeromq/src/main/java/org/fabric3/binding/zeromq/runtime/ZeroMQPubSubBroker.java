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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime;

import java.net.URI;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.container.ContainerException;
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
     * @throws ContainerException if an error occurs creating the subscription
     */
    void subscribe(URI subscriberId, ZeroMQMetadata metadata, ChannelConnection connection, ClassLoader loader) throws ContainerException;

    /**
     * Removes a consumer from the given channel.
     *
     * @param subscriberId the unique subscription id
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @throws ContainerException if an error occurs removing the subscription
     */
    void unsubscribe(URI subscriberId, ZeroMQMetadata metadata) throws ContainerException;

    /**
     * Connects the channel connection to the publisher for the given channel.
     *
     * @param connectionId the unique id for the connection
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @param connection   the producer that dispatches messages to the publisher
     * @param loader       the classloader for the event types being sent
     * @throws ContainerException if an error occurs removing the subscription
     */
    void connect(String connectionId, ZeroMQMetadata metadata, boolean dedicatedThread, ChannelConnection connection, ClassLoader loader)
            throws ContainerException;

    /**
     * Releases a publisher for a channel.
     *
     * @param connectionId the unique id for the connection
     * @param metadata     the ZeroMQ metadata to configure the underlying socket
     * @throws ContainerException if an error occurs removing the publisher
     */
    void release(String connectionId, ZeroMQMetadata metadata) throws ContainerException;

    /**
     * Starts all publishers and subscribers.
     */
    void startAll();

    /**
     * Stops all publishers and subscribers.
     */
    void stopAll();

}

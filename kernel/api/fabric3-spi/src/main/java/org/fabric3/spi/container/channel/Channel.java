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
package org.fabric3.spi.container.channel;

import java.net.URI;

import org.fabric3.spi.model.physical.ChannelSide;

/**
 * An event channel. Responsible for transmitting events from producers to consumers.
 *
 * A logical channel is divided into two physical channels on a runtime: one for receiving events from producers and transmitting them to a binding; and one for
 * receiving events from a binding and transmitting them to consumers. If the channel is not bound, it is collocated and only one physical channel will exist.
 */
public interface Channel {
    /**
     * Returns the channel URI.
     *
     * @return the channel URI
     */
    URI getUri();

    /**
     * Returns the URI of the contribution this channel was deployed with.
     *
     * @return the contribution URI
     */
    URI getContributionUri();

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
    void attach(EventStreamHandler handler);

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
     * @param uri   the subscription URI
     * @param topic the topic; may be null
     * @return the unsubscribed connection
     */
    ChannelConnection unsubscribe(URI uri, String topic);

    /**
     * Returns if the Channel receives producer events or sends events to consumers.
     *
     * @return if the Channel receives producer events or sends events to consumers
     */
    ChannelSide getChannelSide();

    /**
     * Returns a direct connection to the channel, typically the dispatcher such as a ring buffer.
     *
     * @param topic the topic; may be null
     * @return a direct connection ot the channel
     */
    Object getDirectConnection(String topic);

}
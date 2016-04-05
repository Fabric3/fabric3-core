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

import java.util.Optional;
import java.util.function.Supplier;

import org.fabric3.spi.util.Closeable;

/**
 * Contains one or more event streams for transmitting events to or from a channel. Channel connections may exist between:
 *
 * - A component producer and a channel
 *
 * - A component producer and a channel binding
 *
 * - A channel binding and a channel
 *
 * - A channel and a component consumer
 */
public interface ChannelConnection {

    /**
     * Returns the topic this connection is associated with.
     *
     * @return the topic this connection is associated with; may be null
     */
    String getTopic();

    /**
     * Returns the sequence this connection should receive events from a channel.
     *
     * @return the sequence
     */
    int getSequence();

    /**
     * Returns the connection event stream.
     *
     * @return the connection event stream
     */
    EventStream getEventStream();

    /**
     * Returns a supplier that provides a direct connection to the channel, which is typically a dispatcher for collocated channels or a transport API for a
     * binding.
     *
     * @return the supplier
     */
    <T> Optional<Supplier<T>> getDirectConnection();

    /**
     * Returns the closeable used to release resources associated with this stream.
     *
     * @return the closeable
     */
    Closeable getCloseable();

    /**
     * Sets a closeable that can be invoked to release resources associated with this stream.
     *
     * @param closeable the delegate
     */
    void setCloseable(Closeable closeable);

}
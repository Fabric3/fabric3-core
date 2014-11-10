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
package org.fabric3.binding.zeromq.runtime.management;

import java.net.URI;

import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.binding.zeromq.runtime.message.Receiver;
import org.fabric3.binding.zeromq.runtime.message.Sender;
import org.fabric3.binding.zeromq.runtime.message.Subscriber;

/**
 * Exposes ZeroMQ binding infrastructure to the runtime management framework.
 */
public interface ZeroMQManagementService {

    /**
     * Registers a {@link Subscriber} for management.
     *
     * @param channelName  the channel the subscriber is listening on
     * @param subscriberId the unique subscriber id
     * @param subscriber   the subscriber
     */
    void register(String channelName, URI subscriberId, Subscriber subscriber);

    /**
     * Unregisters a subscriber.
     *
     * @param channelName  the channel the subscriber is listening on
     * @param subscriberId the unique subscriber id
     */
    void unregister(String channelName, URI subscriberId);

    /**
     * Registers a {@link Publisher} for management.
     *
     * @param channelName the channel the publisher is sending messages to
     * @param publisher   the publisher
     */
    void register(String channelName, Publisher publisher);

    /**
     * Unregisters a {@link Publisher}.
     *
     * @param channelName the channel the publisher is sending messages to
     */
    void unregister(String channelName);

    /**
     * Registers a {@link Sender} for management.
     *
     * @param id     the sender id
     * @param sender the sender
     */
    void registerSender(String id, Sender sender);

    /**
     * Unregisters a {@link Sender}.
     *
     * @param id the sender id
     */
    void unregisterSender(String id);

    /**
     * Registers a {@link Receiver} for management.
     *
     * @param id       the receiver id
     * @param receiver the receiver
     */
    void registerReceiver(String id, Receiver receiver);

    /**
     * Unregisters a {@link Receiver}.
     *
     * @param id the receiver id
     */
    void unregisterReceiver(String id);
}

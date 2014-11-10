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
package org.fabric3.binding.web.runtime.channel;

/**
 * Manages {@link ChannelSubscriber} and {@link ChannelPublisher} instances.
 */
public interface PubSubManager {

    /**
     * Registers a publisher.
     *
     * @param path      the relative path of the channel the publisher sends events to.
     * @param publisher the publisher
     */
    void register(String path, ChannelPublisher publisher);

    /**
     * Registers a subscriber.
     *
     * @param path       the relative path of the channel the subscriber listens to.
     * @param subscriber the publisher
     */
    void register(String path, ChannelSubscriber subscriber);

    /**
     * Removes a publisher.
     *
     * @param path the relative path of the channel the subscriber listens to.
     * @return returns the publisher or null if not found
     */
    ChannelPublisher unregisterPublisher(String path);

    /**
     * Removes a subscriber.
     *
     * @param path the relative path of the channel the subscriber listens to.
     * @return returns the subscriber or null if not found
     */
    ChannelSubscriber unregisterSubscriber(String path);

    /**
     * Returns the publisher for the given channel.
     *
     * @param path the relative channel path
     * @return the publisher
     */
    ChannelPublisher getPublisher(String path);

    /**
     * Returns the subscriber for the given channel.
     *
     * @param path the relative channel path
     * @return the subscriber
     */
    ChannelSubscriber getSubscriber(String path);

}

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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class PubSubManagerImpl implements PubSubManager {
    private Map<String, ChannelPublisher> publishers = new ConcurrentHashMap<>();
    private Map<String, ChannelSubscriber> subscribers = new ConcurrentHashMap<>();


    public void register(String path, ChannelPublisher publisher) {
        publishers.put(path, publisher);
    }

    public void register(String path, ChannelSubscriber subscriber) {
        subscribers.put(path, subscriber);
    }

    public ChannelPublisher unregisterPublisher(String path) {
        return publishers.remove(path);
    }

    public ChannelSubscriber unregisterSubscriber(String path) {
        return subscribers.remove(path);
    }

    public ChannelPublisher getPublisher(String path) {
        return publishers.get(path);
    }

    public ChannelSubscriber getSubscriber(String path) {
        return subscribers.get(path);
    }

}

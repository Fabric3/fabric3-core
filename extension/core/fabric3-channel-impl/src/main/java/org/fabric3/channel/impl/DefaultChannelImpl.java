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
package org.fabric3.channel.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.PassThroughHandler;
import org.fabric3.spi.model.physical.ChannelSide;

/**
 * The default Channel implementation.
 *
 * This implementation supports topics. Handlers are organized by topic name to ensure events are received and sent on the appropriate topic.
 */
public class DefaultChannelImpl implements Channel {
    private URI uri;
    private URI contributionUri;
    private ExecutorService executorService;
    private final ChannelSide channelSide;

    private TopicHandlers defaultTopicHandlers;
    private Map<String, TopicHandlers> handlerMap = new HashMap<>();   // map of topics to handlers

    public DefaultChannelImpl(URI uri, ChannelSide channelSide, URI contributionUri) {
        this.uri = uri;
        this.channelSide = channelSide;
        this.contributionUri = contributionUri;
        SyncFanOutHandler fanOutHandler = new SyncFanOutHandler();
        defaultTopicHandlers = new TopicHandlers(fanOutHandler, null);
    }

    public DefaultChannelImpl(URI uri, ChannelSide channelSide, URI contributionUri, ExecutorService executorService) {
        this.uri = uri;
        this.channelSide = channelSide;
        this.contributionUri = contributionUri;
        this.executorService = executorService;
        AsyncFanOutHandler fanOutHandler = new AsyncFanOutHandler(executorService);
        defaultTopicHandlers = new TopicHandlers(fanOutHandler, null);
    }

    public URI getUri() {
        return uri;
    }

    public URI getContributionUri() {
        return contributionUri;
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

    public void addHandler(EventStreamHandler handler) {
        if (defaultTopicHandlers.headHandler == null) {
            defaultTopicHandlers.headHandler = handler;
            defaultTopicHandlers.inHandler.setNext(handler);
        } else {
            defaultTopicHandlers.tailHandler.setNext(handler);
        }
        defaultTopicHandlers.tailHandler = handler;
        defaultTopicHandlers.tailHandler.setNext(defaultTopicHandlers.fanOutHandler);
    }

    public void removeHandler(EventStreamHandler handler) {
        EventStreamHandler current = defaultTopicHandlers.headHandler;
        EventStreamHandler previous = null;
        while (current != null) {
            if (current == handler) {
                if (defaultTopicHandlers.headHandler == current) {
                    defaultTopicHandlers.headHandler = current.getNext();
                }
                if (defaultTopicHandlers.tailHandler == current) {
                    defaultTopicHandlers.tailHandler = previous == null ? defaultTopicHandlers.headHandler : previous;
                }
                if (previous != null) {
                    previous.setNext(current.getNext());
                }
                defaultTopicHandlers.inHandler.setNext(defaultTopicHandlers.headHandler);
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    public void attach(EventStreamHandler handler) {
        handler.setNext(defaultTopicHandlers.inHandler);
    }

    public void attach(ChannelConnection connection) {
        EventStream stream = connection.getEventStream();
        String topic = connection.getTopic();

        TopicHandlers topicHandlers = getTopicHandlers(topic, true);
        stream.getTailHandler().setNext(topicHandlers.inHandler);
    }

    public void subscribe(URI uri, ChannelConnection connection) {
        String topic = connection.getTopic();
        TopicHandlers topicHandlers = getTopicHandlers(topic, true);
        topicHandlers.fanOutHandler.addConnection(uri, connection);
    }

    public ChannelConnection unsubscribe(URI uri, String topic) {
        return getTopicHandlers(topic, false).fanOutHandler.removeConnection(uri);
    }

    public ChannelSide getChannelSide() {
        return channelSide;
    }

    public Object getDirectConnection(String topic) {
        return getTopicHandlers(topic, true).headHandler;
    }

    private TopicHandlers getTopicHandlers(String topic, boolean create) {
        if (topic == null) {
            return defaultTopicHandlers;
        }
        TopicHandlers topicHandlers = handlerMap.get(topic);
        if (topicHandlers == null) {
            if (create) {
                FanOutHandler fanOutHandler = executorService != null ? new AsyncFanOutHandler(executorService) : new SyncFanOutHandler();
                topicHandlers = new TopicHandlers(fanOutHandler, topic);
                handlerMap.put(topic, topicHandlers);
            } else {
                throw new Fabric3Exception("Handlers not registered for topic: " + topic);
            }
        }
        return topicHandlers;
    }

    private class TopicHandlers {
        String topic;
        EventStreamHandler inHandler;
        FanOutHandler fanOutHandler;
        EventStreamHandler headHandler;
        EventStreamHandler tailHandler;

        public TopicHandlers(FanOutHandler fanOutHandler, String topic) {
            this.topic = topic;
            inHandler = new PassThroughHandler();
            this.fanOutHandler = fanOutHandler;
            inHandler.setNext(this.fanOutHandler);
        }

    }
}
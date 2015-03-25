package org.fabric3.node.domain;

import org.fabric3.api.ChannelContext;

/**
 *
 */
public class ChannelContextImpl implements ChannelContext {
    private String name;
    private String topic;
    private ChannelResolver resolver;

    public ChannelContextImpl(String name, String topic, ChannelResolver resolver) {
        this.name = name;
        this.topic = topic;
        this.resolver = resolver;
    }

    public <T> T getProducer(Class<T> type) {
        return resolver.getProducer(type, name);
    }

    public <T> T getProducer(Class<T> type, String topic) {
        return resolver.getProducer(type, name, topic);
    }

    public <T> T getConsumer(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public <T> T getConsumer(Class<T> type, String topic) {
        throw new UnsupportedOperationException();
    }
}

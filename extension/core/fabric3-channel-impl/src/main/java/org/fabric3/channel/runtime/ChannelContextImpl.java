package org.fabric3.channel.runtime;

import java.util.function.Consumer;

import org.fabric3.api.ChannelContext;
import org.fabric3.spi.container.channel.ChannelResolver;
import org.fabric3.spi.util.Closeable;

/**
 *
 */
public class ChannelContextImpl implements ChannelContext {
    private String name;
    private ChannelResolver resolver;

    /**
     * Constructor.
     *
     * @param name     the channel name
     * @param resolver the resolver
     */
    public ChannelContextImpl(String name, ChannelResolver resolver) {
        this.name = name;
        this.resolver = resolver;
    }

    public <T> T getProducer(Class<T> type) {
        return resolver.getProducer(type, name);
    }

    public <T> T getProducer(Class<T> type, String topic) {
        return resolver.getProducer(type, name, topic);
    }

    public <T> T getConsumer(Class<T> type) {
        return resolver.getConsumer(type, name);
    }

    public <T> T getConsumer(Class<T> type, String topic) {
        return resolver.getConsumer(type, name, topic);
    }

    public <T> Object subscribe(Class<T> type, String id, String topic, Consumer<T> consumer) {
        return resolver.subscribe(type, name, id, topic, consumer);
    }

    public <T> Object subscribe(Class<T> type, Consumer<T> consumer) {
        return resolver.subscribe(type, name, null, null, consumer);
    }

    public void close(Object object) {
        if (object instanceof Closeable) {
            ((Closeable) object).close();
        }
    }
}

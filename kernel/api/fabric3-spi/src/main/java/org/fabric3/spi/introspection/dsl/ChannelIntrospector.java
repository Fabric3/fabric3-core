package org.fabric3.spi.introspection.dsl;

import org.fabric3.api.model.type.component.Channel;

/**
 * Introspects a channel defined using the DSL.
 */
public interface ChannelIntrospector {

    /**
     * Introspects a channel. The model object may be enhanced with additional data.
     *
     * @param channel the channel
     */
    void introspect(Channel channel);

}

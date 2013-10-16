package org.fabric3.spi.container.channel;

import org.fabric3.api.model.type.contract.DataType;

/**
 * Creates an {@link EventStreamHandler} that converts events from one type to another.
 */
public interface TransformerHandlerFactory {

    /**
     * Creates the handler for converting an event type.
     *
     * @param source the source type
     * @param target the target type
     * @param loader the classloader or loading target types
     * @return the handler
     * @throws HandlerCreationException
     *          if there is an exception creating the handler
     */
    EventStreamHandler createHandler(DataType<?> source, DataType<?> target, ClassLoader loader) throws HandlerCreationException;
}

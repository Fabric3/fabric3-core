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
package org.fabric3.fabric.container.handler;

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.container.channel.HandlerCreationException;
import org.fabric3.spi.container.channel.TransformerHandlerFactory;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class TransformerHandlerFactoryImpl implements TransformerHandlerFactory {
    private TransformerRegistry registry;

    public TransformerHandlerFactoryImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public EventStreamHandler createHandler(DataType source, DataType target, List<Class<?>> eventTypes, ClassLoader loader) throws HandlerCreationException {
        try {
            // Find a transformer that can convert from a type supported by the source component or binding to one supported by the target component
            // or binding. A search is performed by iterating the supported source and target types in order of preference.
            Transformer<Object, Object> transformer = (Transformer<Object, Object>) registry.getTransformer(source, target, eventTypes, eventTypes);
            if (transformer == null) {
                throw new NoTransformerException("No transformer found for event types: " + source + "," + target);
            }
            return new TransformerHandler(transformer, loader);
        } catch (TransformationException e) {
            throw new HandlerCreationException(e);
        }
    }


}

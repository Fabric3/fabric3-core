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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * An {@link EventStreamHandler} that transforms wrapped events to a type expected by the consumer.
 * <p/>
 * If the event is not wrapped, no transformation is done. This implementation also lazily creates transformers and caches them for reuse as binding contexts
 * such as JAXB can be expensive to create.
 */
public class TransformerHandler implements EventStreamHandler {
    private EventStreamHandler next;
    private DataType targetType;
    private TransformerRegistry registry;
    private List<Class<?>> typeList;

    private Map<DataType, Transformer<Object, Object>> cache;

    public TransformerHandler(DataType targetType, TransformerRegistry registry) {
        this.targetType = targetType;
        this.registry = registry;
        Class<?> clazz = targetType.getType();
        typeList = cast(Collections.singletonList(clazz));
    }

    @SuppressWarnings({"unchecked"})
    public void handle(Object event, boolean endOfBatch) {
        if (event instanceof EventWrapper) {
            if (cache == null) {
                cache = new ConcurrentHashMap<>();
            }
            EventWrapper wrapper = (EventWrapper) event;

            // check to see if the transformed event is cached
            Object content = ((EventWrapper) event).getEvent(targetType);
            if (content == null) {
                try {
                    DataType type = wrapper.getType();
                    ClassLoader loader = targetType.getClass().getClassLoader();
                    Transformer<Object, Object> transformer = cache.get(type);
                    if (transformer == null) {
                        transformer = (Transformer<Object, Object>) registry.getTransformer(type, targetType, typeList, typeList);
                        cache.put(type, transformer);
                    }
                    content = transformer.transform(wrapper.getEvent(), loader);
                    wrapper.cache(targetType, content);
                } catch (TransformationException e) {
                    throw new ServiceRuntimeException(e);
                }
            }
            event = content;
        }
        next.handle(event, endOfBatch);
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }
}

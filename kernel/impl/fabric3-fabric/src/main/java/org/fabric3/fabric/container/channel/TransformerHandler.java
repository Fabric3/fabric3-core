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
package org.fabric3.fabric.container.channel;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.EventStreamHandler;
import org.fabric3.spi.transform.Transformer;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Converts the event to a target format by delegating to a transformer.
 */
public class TransformerHandler implements EventStreamHandler {
    private Transformer<Object, Object> transformer;
    private ClassLoader loader;
    private EventStreamHandler next;

    /**
     * Constructor.
     *
     * @param transformer the transformer
     * @param loader      the event type classloader
     */
    public TransformerHandler(Transformer<Object, Object> transformer, ClassLoader loader) {
        this.transformer = transformer;
        this.loader = loader;
    }

    public void handle(Object event, boolean endOfBatch) {
        try {
            Object o = transformer.transform(event, loader);
            next.handle(o, endOfBatch);
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

}

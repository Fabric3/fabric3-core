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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.zeromq.runtime.handler;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.zeromq.runtime.message.Publisher;
import org.fabric3.spi.container.channel.EventStreamHandler;

/**
 * Forwards events to a ZeroMQ publisher.
 */
public class PublisherHandler implements EventStreamHandler {
    private Publisher publisher;

    public PublisherHandler(Publisher publisher) {
        this.publisher = publisher;
    }

    public void handle(Object event, boolean endOfBatch) {
        if ((event instanceof byte[])) {
            // single frame message
            publisher.publish((byte[]) event);
        } else if (event instanceof byte[][]) {
            // multi-frame message
            publisher.publish((byte[][]) event);
        } else {
            throw new ServiceRuntimeException("Event must be serialized: " + event);
        }
    }

    public void setNext(EventStreamHandler next) {
        throw new IllegalStateException("This handler must be the last one in the handler sequence");
    }

    public EventStreamHandler getNext() {
        return null;
    }
}

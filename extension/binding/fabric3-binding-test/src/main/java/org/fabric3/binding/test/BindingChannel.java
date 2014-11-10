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
package org.fabric3.binding.test;

import java.net.URI;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Wire;

/**
 * Implementations route messages to a service destination.
 */
public interface BindingChannel {

    /**
     * Registers a wire to a service destination
     *
     * @param uri         the destination uri
     * @param wire        the wire
     * @param callbackUri the callback uri or null
     */
    void registerDestinationWire(URI uri, Wire wire, URI callbackUri);

    /**
     * Sends a message to the destination, invoking the given operation. Note overloaded operations are not supported
     *
     * @param destination the destination uri
     * @param operation   the operation name
     * @param msg         the message
     * @return the response
     */
    Message send(URI destination, String operation, Message msg);

}

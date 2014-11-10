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
package org.fabric3.spi.container.binding.handler;

import javax.xml.namespace.QName;

import org.fabric3.spi.container.invocation.Message;

/**
 * Invoked when a message is sent or received over a specific binding that supports handlers. Handlers may populate the message with transport or
 * contextual data and vice-versa.
 */
public interface BindingHandler<T> {

    /**
     * The fully qualified binding name corresponding to the SCA architected binding name scheme binding.xxxx.
     *
     * @return the fully qualified binding name
     */
    QName getType();

    /**
     * Handles an outbound (reference-side) message.
     *
     * @param context the binding-specific transport context
     * @param message the current message
     */
    void handleOutbound(Message message, T context);

    /**
     * Handles an inbound (service-side) message.
     *
     * @param context the binding-specific transport context
     * @param message the current message
     */
    void handleInbound(T context, Message message);

}

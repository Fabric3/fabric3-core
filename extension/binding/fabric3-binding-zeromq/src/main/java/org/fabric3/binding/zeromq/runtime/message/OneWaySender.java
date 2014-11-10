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
package org.fabric3.binding.zeromq.runtime.message;

import org.fabric3.spi.container.invocation.WorkContext;

/**
 * Implementations dispatch messages over a ZeroMQ socket using a non-blocking one-way pattern. Qualities of service such as reliability may be
 * provided by an implementation.
 */
public interface OneWaySender extends Sender {

    /**
     * Dispatches a message to a service in a non-blocking fashion.
     *
     * @param message the serialized message
     * @param index   the operation index used to determine which intercept chain to dispatch the message to
     * @param context the current work context
     */
    void send(byte[] message, int index, WorkContext context);
}

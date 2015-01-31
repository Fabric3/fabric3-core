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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.builder;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 * Establishes (and removes) event channel connections.
 */
public interface ChannelConnector {

    /**
     * Establishes a channel connection from an event source (component producer, channel, or channel binding) to an event target (component consumer,
     * channel, or channel binding).
     *
     * @param definition the connection metadata
     * @throws ContainerException if an error creating the connect is encountered
     */
    void connect(PhysicalChannelConnectionDefinition definition) throws ContainerException;

    /**
     * Removes a channel connection.
     *
     * @param definition the connection metadata
     * @throws ContainerException if an error disconnecting is encountered
     */
    void disconnect(PhysicalChannelConnectionDefinition definition) throws ContainerException;
}
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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;

import org.fabric3.api.host.ContainerException;

/**
 * Manages {@link AdaptiveMessageContainer}s used to receive messages from a JMS provider.
 */
public interface MessageContainerManager {

    /**
     * Returns true if a listener for the service URI is registered.
     *
     * @param uri the container URI
     * @return true if a listener is registered
     */
    boolean isRegistered(URI uri);

    /**
     * Register a container which dispatches inbound JMS messages.
     *
     * @param container the container
     * @throws ContainerException if an error registering the container is encountered
     */
    public void register(AdaptiveMessageContainer container) throws ContainerException;

    /**
     * Unregister a container.
     *
     * @param uri the container URI
     * @throws ContainerException if an error un-registering the container is encountered
     */
    public void unregister(URI uri) throws ContainerException;

}

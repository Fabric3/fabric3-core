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
package org.fabric3.binding.rs.runtime.container;

import java.net.URI;

/**
 * Manages active {@link RsContainer}s.
 */
public interface RsContainerManager {

    /**
     * Registers a container.
     *
     * @param name      the unique container name
     * @param container the container
     */
    void register(URI name, RsContainer container);

    /**
     * Removes a container.
     *
     * @param name the container name
     */
    void unregister(URI name);

    /**
     * Returns a container matching the given name.
     *
     * @param name the container name
     * @return the container or null if not found
     */
    RsContainer get(URI name);

}

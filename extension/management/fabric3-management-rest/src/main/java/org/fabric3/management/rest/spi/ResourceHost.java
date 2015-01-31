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
package org.fabric3.management.rest.spi;

import org.fabric3.api.host.ContainerException;

/**
 * Responsible for dispatching requests to a managed resource.
 */
public interface ResourceHost {

    /**
     * Returns true if the path is registered.
     *
     * @param path the resource path
     * @param verb the HTTP verb for the path as paths may be registered for multiple verbs
     * @return true if the path is registered
     */
    boolean isPathRegistered(String path, Verb verb);

    /**
     * Registers a mapping, making the managed resource available via HTTP.
     *
     * @param mapping the mapping
     * @throws ContainerException if a managed resource has already been registered for the path
     */
    void register(ResourceMapping mapping) throws ContainerException;

    /**
     * Removes mappings for the given resource identifier. Multiple path/verb associations may be removed.
     *
     * @param identifier the identifier
     */
    void unregister(String identifier);

    /**
     * Removes a mapping for the given path.
     *
     * @param path the resource path
     * @param verb the HTTP verb for the path as paths may be registered for multiple verbs
     */
    void unregisterPath(String path, Verb verb);

    /**
     * Dispatches a request directly to a managed resource.
     *
     * @param path   the resource path
     * @param verb   the HTTP verb
     * @param params the resource parameters
     */
    void dispatch(String path, Verb verb, Object[] params);


}

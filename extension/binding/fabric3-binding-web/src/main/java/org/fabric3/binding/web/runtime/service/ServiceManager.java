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
package org.fabric3.binding.web.runtime.service;

import org.fabric3.spi.container.wire.InvocationChain;

/**
 * Manages invocation chains for a service exposed over the websocket binding.
 */
public interface ServiceManager {

    /**
     * Registers the invocation chain for a service.
     *
     * @param path        the path part of the service URI
     * @param chain       the invocation chain
     * @param callbackUri the callback URI. This may be null if the service is unidirectional
     */
    void register(String path, InvocationChain chain, String callbackUri);

    /**
     * Removes the invocation chain for a service.
     *
     * @param path the path part of the service URI
     */
    void unregister(String path);

    /**
     * Returns the invocation chain and callback URI pair for a registered service.
     *
     * @param path the path part of the service URI
     * @return the invocation chain and callback URI pair
     */
    ChainPair get(String path);
}

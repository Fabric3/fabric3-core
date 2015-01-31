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
package org.fabric3.api.host.runtime;

import java.net.URI;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.monitor.Monitorable;

/**
 * A Fabric3 runtime.
 */
public interface Fabric3Runtime extends Monitorable {

    /**
     * Returns the named system component providing the designated service.
     *
     * @param service the service interface required
     * @param uri     the id of the system component
     * @param <I>     the Java type for the service interface
     * @return an implementation of the requested service
     */
    <I> I getComponent(Class<I> service, URI uri);

    /**
     * Returns the default system component providing the designated service.
     *
     * @param service the service interface required
     * @param <I>     the Java type for the service interface
     * @return an implementation of the requested service
     */
    <I> I getComponent(Class<I> service);

    /**
     * Boots core services in the runtime.
     *
     * @throws ContainerException if there is an error initializing the runtime
     */
    void boot() throws ContainerException;

    /**
     * Destroy the runtime. Any further invocations should result in an error.
     *
     * @throws ContainerException if there is an error destroying the runtime
     */
    void destroy() throws ContainerException;

}

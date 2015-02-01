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
package org.fabric3.api.host.runtime;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Manages the lifecycle of a Fabric3 runtime instance.
 */
public interface RuntimeCoordinator {

    /**
     * Returns the runtime state.
     *
     * @return the runtime state
     */
    RuntimeState getState();

    /**
     * Prepares the runtime, synchronizes it with the domain, and places it in a state to receive requests. Equivalent to calling {@link #boot()}, {@link
     * #load()} and {@link #joinDomain()}.
     *
     * @throws Fabric3Exception if an error occurs starting the runtime
     */
    void start() throws Fabric3Exception;

    /**
     * Prepares the runtime by performing bootstrap.
     *
     * @throws Fabric3Exception if an error occurs booting the runtime
     */
    void boot() throws Fabric3Exception;

    /**
     * Loads extensions and performs local recovery.
     *
     * @throws Fabric3Exception if an error occurs loading the runtime
     */
    void load() throws Fabric3Exception;

    /**
     * Performs domain synchronization, domain recovery and places the runtime in a state to receive requests.
     */
    void joinDomain();

    /**
     * Shuts the runtime down, stopping it from receiving requests and detaching it from the domain. In-flight synchronous operations will be allowed to proceed
     * to completion.
     *
     * @throws Fabric3Exception if an error occurs shutting down the runtime
     */
    void shutdown() throws Fabric3Exception;
}



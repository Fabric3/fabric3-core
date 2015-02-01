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
package org.fabric3.spi.container.component;

import org.fabric3.api.host.Fabric3Exception;

/**
 * A component whose implementation instances are managed by a {@link ScopeContainer}. This interface defines callbacks used by the scope container to change
 * the state of an implementation.
 */
public interface ScopedComponent extends AtomicComponent {

    /**
     * Returns true if component instances should be eagerly initialized.
     *
     * @return true if component instances should be eagerly initialized
     */
    boolean isEagerInit();

    /**
     * Create a new implementation instance, fully injected with all property and reference values. The instance's lifecycle callbacks must not have been
     * called.
     *
     * @return a wrapper for a new implementation instance
     * @throws Fabric3Exception if there was a problem instantiating the implementation
     */
    Object createInstance() throws Fabric3Exception;

    /**
     * Starts a component instance. If configured on the implementation, an initialization callback will be performed.
     *
     * @param instance the instance to start
     * @throws Fabric3Exception if there is an error initializing the instance
     */
    void startInstance(Object instance) throws Fabric3Exception;

    /**
     * Stops a component instance. If configured on the implementation, a destruction callback will be performed.
     *
     * @param instance    the instance to start
     * @throws Fabric3Exception if there is an error stopping the instance
     */
    void stopInstance(Object instance) throws Fabric3Exception;

    /**
     * Reinjects the instance with updated references.
     *
     * @param instance the instance
     * @throws Fabric3Exception if there is an error reinjecting the instance
     */
    void reinject(Object instance) throws Fabric3Exception;

}

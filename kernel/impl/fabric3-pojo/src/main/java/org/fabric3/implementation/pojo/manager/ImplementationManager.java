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
package org.fabric3.implementation.pojo.manager;

import org.fabric3.api.host.ContainerException;

/**
 * Returns an injected component instance. This is used by a Component implementation to create new instances of application implementation objects as
 * determined by the component scope's lifecycle.
 */
public interface ImplementationManager {
    /**
     * Creates a new instance of the component. All injected values must be set but any @Init methods must not have been invoked.
     *
     * @return A new component instance
     * @throws ContainerException if there was a problem creating the instance
     */
    Object newInstance() throws ContainerException;

    /**
     * Starts the instance, calling an @Init method if one is configured.
     *
     * @param instance the instance
     * @throws ContainerException if there is an error when calling the initialization method
     */
    void start(Object instance) throws ContainerException;

    /**
     * Stops the instance, calling an @Destroy method if one is configured.
     *
     * @param instance the instance
     * @throws ContainerException if there is an error when calling the initialization method
     */
    void stop(Object instance) throws ContainerException;

    /**
     * Reinjects the instance with any updated references.
     *
     * @param instance the instance
     * @throws ContainerException if an error is raised during reinjection
     */
    void reinject(Object instance) throws ContainerException;

    /**
     * Updates the instance with a new reference proxy.
     *
     * @param instance      the instance
     * @param referenceName the reference name
     */
    void updated(Object instance, String referenceName);

    /**
     * Updates the instance when a reference has been removed.
     *
     * @param instance      the instance
     * @param referenceName the reference name
     */
    void removed(Object instance, String referenceName);

}

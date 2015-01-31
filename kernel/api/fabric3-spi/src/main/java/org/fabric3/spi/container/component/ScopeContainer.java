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
package org.fabric3.spi.container.component;

import javax.xml.namespace.QName;
import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Scope;

/**
 * Manages the lifecycle and visibility component implementations instances.
 */
public interface ScopeContainer {

    /**
     * Returns the Scope that this container supports.
     *
     * @return the Scope that this container supports
     */
    Scope getScope();

    /**
     * Registers a component with the scope.
     *
     * @param component the component to register
     */
    void register(ScopedComponent component);

    /**
     * Unregisters a component with the scope.
     *
     * @param component the component to unregister
     */
    void unregister(ScopedComponent component);

    /**
     * Start a new, non-expiring context. The context will remain active until explicitly stopped.
     *
     * @param deployable the deployable to start the context for
     * @throws ContainerException if an exception starting the context was encountered
     */
    void startContext(QName deployable) throws ContainerException;

    /**
     * Stop the context associated with the current work context.
     *
     * @param deployable the deployable to start the context for
     * @throws ContainerException if there is an error stopping the context
     */
    void stopContext(QName deployable) throws ContainerException;

    /**
     * Returns an instance associated with the current scope context, creating one if necessary
     *
     * @param component the component
     * @return the instance
     * @throws ContainerException if there was a problem instantiating the target instance
     */
    Object getInstance(ScopedComponent component) throws ContainerException;

    /**
     * Return am instance after use (for example, after invoking the instance).
     *
     * @param component the component
     * @param instance  the instance
     * @throws ContainerException if there was a problem returning the target instance
     */
    void releaseInstance(ScopedComponent component, Object instance) throws ContainerException;

    /**
     * Returns a snapshot of the component instances that are active and currently managed by the scope container.
     *
     * @param component the component
     * @return the active instances
     */
    List<Object> getActiveInstances(ScopedComponent component);

    /**
     * Reinjects all live instances with updated wires
     *
     * @throws ContainerException if an error occurs during reinjection
     */
    void reinject() throws ContainerException;

}

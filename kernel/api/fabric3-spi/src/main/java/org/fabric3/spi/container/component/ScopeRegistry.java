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

import org.fabric3.api.model.type.component.Scope;

/**
 * Manages {@link ScopeContainer}s in the runtime
 */
public interface ScopeRegistry {

    /**
     * Returns the scope container for the given scope or null if one not found.
     *
     * @param scope the scope
     * @return the scope container for the given scope or null if one not found
     */
    ScopeContainer getScopeContainer(Scope scope);

    /**
     * Returns the scope container for the given scope name or null if one not found.
     *
     * @param scopeName the scope name
     * @return the scope container for the given scope or null if one not found
     */
    ScopeContainer getScopeContainer(String scopeName);

    /**
     * Register a scope container with this registry.
     *
     * @param container the container to register
     */
    void register(ScopeContainer container);

    /**
     * Unregister a scope container from this registry.
     *
     * @param container the container to unregister
     */
    void unregister(ScopeContainer container);
}

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
package org.fabric3.jpa.override;

import java.net.URI;

import org.fabric3.jpa.api.PersistenceOverrides;

/**
 * Manages persistence context overrides.
 */
public interface OverrideRegistry {

    /**
     * Registers a set of persistence overrides contained by a given contribution. The overrides will be held until the contribution is uninstalled.
     *
     * @param contributionURI the contribution URI
     * @param overrides       the overrides
     * @throws DuplicateOverridesException if a set of overrides for the persistence context are already registered
     */
    void register(URI contributionURI, PersistenceOverrides overrides) throws DuplicateOverridesException;

    /**
     * Resolves the overrides for the persistence context or null if none are registered.
     *
     * @param unitName the persistence context name
     * @return the overrides for the persistence context or null if none are registered
     */
    PersistenceOverrides resolve(String unitName);

}
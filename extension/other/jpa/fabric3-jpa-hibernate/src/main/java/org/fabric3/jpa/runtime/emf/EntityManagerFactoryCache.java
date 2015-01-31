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
package org.fabric3.jpa.runtime.emf;

import javax.persistence.EntityManagerFactory;
import java.net.URI;

import org.fabric3.api.host.ContainerException;

/**
 * A cache of EntityManagerFactory instances.
 */
public interface EntityManagerFactoryCache {

    /**
     * Returns the EntityManagerFactory for the given persistence unit name
     *
     * @param unitName the persistence unit name
     * @return the EntityManagerFactory or null if one has not been created
     */
    EntityManagerFactory get(String unitName);

    /**
     * Caches an EntityManagerFactory.
     *
     * @param uri      the URI of the contribution the persistence unit is defined in
     * @param unitName the persistence unit name
     * @param factory      the EntityManagerFactory to cache
     * @throws ContainerException if there is an error caching the EntityManagerFactory
     */
    void put(URI uri, String unitName, EntityManagerFactory factory) throws ContainerException;

}

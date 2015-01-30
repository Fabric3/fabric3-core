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
package org.fabric3.cache.spi;

import org.fabric3.spi.container.ContainerException;

/**
 * Implementations manage cache resources for a provider on a runtime.
 */
public interface CacheManager<T extends PhysicalCacheResourceDefinition> {

    /**
     * Returns a live, thread-safe reference to a cache or null if one is not defined for the given name.
     *
     * @param name    the cache name
     * @param <CACHE> the cache reference type
     * @return the cache reference or null
     */
    <CACHE> CACHE getCache(String name);

    /**
     * Creates resources for a cache.
     *
     * @param configuration the cache configuration
     * @throws ContainerException if there is an error creating the cache resources
     */
    void create(T configuration) throws ContainerException;

    /**
     * Removes resources for a cache.
     *
     * @param configuration the cache configuration
     * @throws ContainerException if there is an error removing the cache resources
     */
    void remove(T configuration) throws ContainerException;

}

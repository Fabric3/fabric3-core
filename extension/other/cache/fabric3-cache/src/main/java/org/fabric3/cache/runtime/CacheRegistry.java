package org.fabric3.cache.runtime;

/**
 * Returns live references to caches configured on a runtime.
 *
 * @version $Rev$ $Date$
 */
public interface CacheRegistry {

    /**
     * Returns a live, thread-safe reference to a cache or null if one is not defined for the given name.
     *
     * @param name    the cache name
     * @param <CACHE> the cache reference type
     * @return the cache reference or null
     */
    <CACHE> CACHE getCache(String name);

}

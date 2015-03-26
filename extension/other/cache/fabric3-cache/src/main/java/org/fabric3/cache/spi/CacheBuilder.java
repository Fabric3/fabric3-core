package org.fabric3.cache.spi;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Specialized builder for a runtime cache. Implementations create and remove cache resources on a runtime.
 */
public interface CacheBuilder<R extends PhysicalCacheResource> {

    /**
     * Creates cache resources on a runtime.
     *
     * @param definition the cache definition
     * @throws Fabric3Exception If unable to build the resource
     */
    void build(R definition) throws Fabric3Exception;

    /**
     * Removes cache resources on a runtime.
     *
     * @param definition the physical resource definition
     * @throws Fabric3Exception If unable to remove the resource
     */
    void remove(R definition) throws Fabric3Exception;

}


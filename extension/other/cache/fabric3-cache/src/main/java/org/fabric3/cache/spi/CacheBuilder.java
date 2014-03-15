package org.fabric3.cache.spi;

import org.fabric3.spi.container.builder.BuildException;

/**
 * Specialized builder for a runtime cache. Implementations create and remove cache resources on a runtime.
 */
public interface CacheBuilder<R extends PhysicalCacheResourceDefinition> {

    /**
     * Creates cache resources on a runtime.
     *
     * @param definition the cache definition
     * @throws BuildException If unable to build the resource
     */
    void build(R definition) throws BuildException;

    /**
     * Removes cache resources on a runtime.
     *
     * @param definition the physical resource definition
     * @throws BuildException If unable to remove the resource
     */
    void remove(R definition) throws BuildException;

}


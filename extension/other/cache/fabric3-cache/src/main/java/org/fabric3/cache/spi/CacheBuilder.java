package org.fabric3.cache.spi;

import org.fabric3.spi.builder.BuilderException;

/**
 * Specialized builder for a runtime cache. Implementations create and remove cache resources on a runtime.
 *
 * @version $Rev$ $Date$
 */
public interface CacheBuilder<R extends PhysicalCacheResourceDefinition> {

    /**
     * Creates cache resources on a runtime.
     *
     * @param definition the cache definition
     * @throws BuilderException If unable to build the resource
     */
    void build(R definition) throws BuilderException;

    /**
     * Removes cache resources on a runtime.
     *
     * @param definition the physical resource definition
     * @throws BuilderException If unable to remove the resource
     */
    void remove(R definition) throws BuilderException;

}


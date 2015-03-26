package org.fabric3.cache.provision;

import org.fabric3.spi.model.physical.PhysicalWireTarget;

/**
 * Defines a connection to a cache for a component resource.
 */
public class CacheWireTarget extends PhysicalWireTarget {
    private String cacheName;

    public CacheWireTarget(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }

    @Override
    public boolean isOptimizable() {
        return true;
    }
}

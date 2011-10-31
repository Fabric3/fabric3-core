package org.fabric3.cache.provision;

import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Defines a connection to a cache for a component resource.
 *
 * @version $Rev$ $Date$
 */
public class CacheTargetDefinition extends PhysicalTargetDefinition {
    private static final long serialVersionUID = -8395954983459809876L;
    private String cacheName;

    public CacheTargetDefinition(String cacheName) {
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

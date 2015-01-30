package org.fabric3.cache.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.cache.spi.CacheManager;
import org.fabric3.cache.spi.CacheResource;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class CacheRegistryImpl implements CacheRegistry {
    private Map<Class<CacheResource>, CacheManager<?>> managers = new ConcurrentHashMap<>();

    @Reference(required = false)
    public void setManagers(Map<Class<CacheResource>, CacheManager<?>> managers) {
        this.managers = managers;
    }

    @SuppressWarnings({"unchecked"})
    public <CACHE> CACHE getCache(String name) {
        for (CacheManager<?> manager : managers.values()) {
            Object cache = manager.getCache(name);
            if (cache != null) {
                return (CACHE) cache;
            }
        }
        return null;
    }

}

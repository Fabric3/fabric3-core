package org.fabric3.cache.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.cache.spi.CacheManager;
import org.fabric3.cache.spi.CacheResourceDefinition;

/**
 *
 */
public class CacheRegistryImpl implements CacheRegistry {
    private Map<Class<CacheResourceDefinition>, CacheManager<?>> managers = new ConcurrentHashMap<Class<CacheResourceDefinition>, CacheManager<?>>();

    @Reference(required = false)
    public void setManagers(Map<Class<CacheResourceDefinition>, CacheManager<?>> managers) {
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

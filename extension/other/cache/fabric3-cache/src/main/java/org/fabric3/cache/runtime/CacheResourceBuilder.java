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

package org.fabric3.cache.runtime;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.cache.provision.PhysicalCacheSetDefinition;
import org.fabric3.cache.spi.CacheBuilder;
import org.fabric3.cache.spi.PhysicalCacheResourceDefinition;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class CacheResourceBuilder implements ResourceBuilder<PhysicalCacheSetDefinition> {
    private Map<Class<?>, CacheBuilder<?>> builders = new HashMap<>();

    @Reference(required = false)
    public void setBuilders(Map<Class<?>, CacheBuilder<?>> builders) {
        this.builders = builders;
    }

    @SuppressWarnings({"unchecked"})
    public void build(PhysicalCacheSetDefinition definition) {
        for (PhysicalCacheResourceDefinition cacheDefinition : definition.getDefinitions()) {
            CacheBuilder builder = getCacheBuilder(cacheDefinition);
            builder.build(cacheDefinition);
        }
    }

    @SuppressWarnings({"unchecked"})
    public void remove(PhysicalCacheSetDefinition definition) {
        for (PhysicalCacheResourceDefinition cacheDefinition : definition.getDefinitions()) {
            CacheBuilder builder = getCacheBuilder(cacheDefinition);
            builder.remove(cacheDefinition);
        }
    }

    private CacheBuilder<?> getCacheBuilder(PhysicalResourceDefinition cacheDefinition) {
        Class<? extends PhysicalResourceDefinition> type = cacheDefinition.getClass();
        CacheBuilder<?> builder = builders.get(type);
        if (builder == null) {
            throw new Fabric3Exception("Cache builder not found for type: " + type);
        }
        return builder;
    }


}

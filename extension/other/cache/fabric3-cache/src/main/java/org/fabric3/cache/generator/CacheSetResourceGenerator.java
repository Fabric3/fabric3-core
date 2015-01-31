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
package org.fabric3.cache.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.ContainerException;
import org.fabric3.cache.model.CacheSetResource;
import org.fabric3.cache.provision.PhysicalCacheSetDefinition;
import org.fabric3.cache.spi.CacheResource;
import org.fabric3.cache.spi.CacheResourceGenerator;
import org.fabric3.cache.spi.PhysicalCacheResourceDefinition;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates a {@link }PhysicalCacheSetDefinition} for a set of cache configurations.
 */
@EagerInit
public class CacheSetResourceGenerator implements ResourceGenerator<CacheSetResource> {
    private Map<Class<?>, CacheResourceGenerator> generators = new HashMap<>();

    @Reference(required = false)
    public void setGenerators(Map<Class<?>, CacheResourceGenerator> generators) {
        this.generators = generators;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalCacheSetDefinition generateResource(LogicalResource<CacheSetResource> resource) throws ContainerException {
        PhysicalCacheSetDefinition definitions = new PhysicalCacheSetDefinition();
        List<CacheResource> configurations = resource.getDefinition().getDefinitions();
        for (CacheResource definition : configurations) {
            CacheResourceGenerator generator = getGenerator(definition);
            PhysicalCacheResourceDefinition physicalResourceDefinition = generator.generateResource(definition);
            definitions.addDefinition(physicalResourceDefinition);
        }
        return definitions;
    }

    private CacheResourceGenerator getGenerator(CacheResource configuration) throws ContainerException {
        Class<? extends CacheResource> type = configuration.getClass();
        CacheResourceGenerator generator = generators.get(type);
        if (generator == null) {
            throw new ContainerException("Cache resource generator not found for type : " + type);
        }
        return generator;
    }
}

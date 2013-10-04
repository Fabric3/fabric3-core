/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.cache.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.cache.model.CacheSetResourceDefinition;
import org.fabric3.cache.provision.PhysicalCacheSetDefinition;
import org.fabric3.cache.spi.CacheResourceDefinition;
import org.fabric3.cache.spi.CacheResourceGenerator;
import org.fabric3.cache.spi.PhysicalCacheResourceDefinition;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.resource.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * Generates a {@link }PhysicalCacheSetDefinition} for a set of cache configurations.
 */
@EagerInit
public class CacheSetResourceGenerator implements ResourceGenerator<CacheSetResourceDefinition> {
    private Map<Class<?>, CacheResourceGenerator> generators = new HashMap<Class<?>, CacheResourceGenerator>();

    @Reference(required = false)
    public void setGenerators(Map<Class<?>, CacheResourceGenerator> generators) {
        this.generators = generators;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalCacheSetDefinition generateResource(LogicalResource<CacheSetResourceDefinition> resource) throws GenerationException {
        PhysicalCacheSetDefinition definitions = new PhysicalCacheSetDefinition();
        List<CacheResourceDefinition> configurations = resource.getDefinition().getDefinitions();
        for (CacheResourceDefinition definition : configurations) {
            CacheResourceGenerator generator = getGenerator(definition);
            PhysicalCacheResourceDefinition physicalResourceDefinition = generator.generateResource(definition);
            definitions.addDefinition(physicalResourceDefinition);
        }
        return definitions;
    }

    private CacheResourceGenerator getGenerator(CacheResourceDefinition configuration) throws GenerationException {
        Class<? extends CacheResourceDefinition> type = configuration.getClass();
        CacheResourceGenerator generator = generators.get(type);
        if (generator == null) {
            throw new GenerationException("Cache resource generator not found for type : " + type);
        }
        return generator;
    }
}

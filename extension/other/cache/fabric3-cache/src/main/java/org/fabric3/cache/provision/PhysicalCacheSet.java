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

package org.fabric3.cache.provision;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.cache.spi.PhysicalCacheResource;
import org.fabric3.spi.model.physical.PhysicalResource;

/**
 *
 */
public class PhysicalCacheSet extends PhysicalResource {
    private List<PhysicalCacheResource> cacheResources = new ArrayList<>();

    public List<PhysicalCacheResource> getCacheResources() {
        return cacheResources;
    }

    public void addDefinition(PhysicalCacheResource cacheResource) {
        cacheResources.add(cacheResource);
    }
}





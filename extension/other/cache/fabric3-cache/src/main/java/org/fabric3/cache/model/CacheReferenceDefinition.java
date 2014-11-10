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

package org.fabric3.cache.model;

import org.fabric3.api.model.type.component.ResourceReferenceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * A reference to a cache resource.
 */
public class CacheReferenceDefinition extends ResourceReferenceDefinition {
    private static final long serialVersionUID = 7840284656807493613L;

    private String cacheName;

    public CacheReferenceDefinition(String name, ServiceContract serviceContract, boolean optional, String cacheName) {
        super(name, serviceContract, optional);
        this.cacheName = cacheName;
    }

    public String getCacheName() {
        return cacheName;
    }
}

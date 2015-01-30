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
package org.fabric3.cache.spi;

import org.fabric3.api.model.type.component.Resource;

/**
 * Defines a cache. Providers subclass to provide specific cache configuration.
 */
public abstract class CacheResource extends Resource {
    private static final long serialVersionUID = -6743311265670833364L;
    private String cacheName;

    public void setCacheName(String name) {
        this.cacheName = name;
    }

    public String getCacheName() {
        return cacheName;
    }
}

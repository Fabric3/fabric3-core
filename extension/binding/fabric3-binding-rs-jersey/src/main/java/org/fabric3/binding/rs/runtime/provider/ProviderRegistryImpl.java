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
package org.fabric3.binding.rs.runtime.provider;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ProviderRegistryImpl implements ProviderRegistry {
    private Map<URI, Object> globalFilters = new HashMap<>();
    private Map<Class<? extends Annotation>, Map<URI, Object>> namedFilters = new HashMap<>();

    public void registerGlobalProvider(URI uri, Object filter) {
        globalFilters.put(uri, filter);
    }

    public Collection<Object> getGlobalProvider() {
        return globalFilters.values();
    }

    public void registerNameFilter(URI filterUri, Class<? extends Annotation> annotation, Object filter) {
        Map<URI, Object> map = namedFilters.get(annotation);
        if (map == null) {
            map = new HashMap<>();
            namedFilters.put(annotation, map);
        }
        map.put(filterUri, filter);
    }

    public Collection<Object> getNameFilters(Class<? extends Annotation> annotation) {
        Map<URI, Object> filters = namedFilters.get(annotation);
        if (filters == null) {
            return null;
        }
        return filters.values();
    }

    public Object unregisterGlobalFilter(URI filterUri) {
        return globalFilters.remove(filterUri);
    }

    public Object unregisterNameFilter(URI filterUri, Class<? extends Annotation> annotation) {
        Map<URI, Object> filters = namedFilters.get(annotation);
        if (filters == null) {
            return null;
        }

        Object filter = filters.remove(filterUri);
        if (filters.isEmpty()) {
            namedFilters.remove(annotation);
        }
        return filter;
    }
}

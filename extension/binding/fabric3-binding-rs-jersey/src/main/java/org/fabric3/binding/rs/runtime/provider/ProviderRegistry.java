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

/**
 * Manages JAX-RS providers such as filters and context resolver providers.
 */
public interface ProviderRegistry {

    /**
     * Registers a global filter.
     *
     * @param uri    a unique filter identifier
     * @param filter the filter
     */
    void registerGlobalProvider(URI uri, Object filter);

    /**
     * Returns registered global filters.
     *
     * @return the global filters
     */
    Collection<Object> getGlobalProvider();

    /**
     * Registers a name filter.
     *
     * @param filterUri  a unique filter identifier
     * @param annotation the name binding annotation the filter is applied for
     * @param filter     the filter
     */
    void registerNameFilter(URI filterUri, Class<? extends Annotation> annotation, Object filter);

    /**
     * Returns name filters that are applied for the given annotation.
     *
     * @param annotation the name binding annotation the filter is applied for
     * @return the filter
     */
    Collection<Object> getNameFilters(Class<? extends Annotation> annotation);

    /**
     * Unregisters a global filter.
     *
     * @param filterUri the filter identifier
     * @return the response filter or null if one is not registered for the identifier
     */
    Object unregisterGlobalFilter(URI filterUri);

    /**
     * Unregisters a name filter.
     *
     * @param filterUri       a unique filter identifier
     * @param annotationClass the binding the filter is applied for
     * @return the filter or null if one is not registered for the annotation
     */
    Object unregisterNameFilter(URI filterUri, Class<? extends Annotation> annotationClass);

}

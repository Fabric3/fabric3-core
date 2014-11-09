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
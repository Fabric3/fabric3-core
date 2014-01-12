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
package org.fabric3.binding.rs.runtime.filter;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Collection;

/**
 * Manages JAX-RS filters.
 */
public interface FilterRegistry {

    /**
     * Registers a global filter.
     *
     * @param uri    a unique filter identifier
     * @param filter the filter
     */
    void registerGlobalFilter(URI uri, Object filter);

    /**
     * Returns registered global filters.
     *
     * @return the global filters
     */
    Collection<Object> getGlobalFilters();

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

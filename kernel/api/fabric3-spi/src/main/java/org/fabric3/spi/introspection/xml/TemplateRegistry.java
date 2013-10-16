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
package org.fabric3.spi.introspection.xml;

import java.net.URI;

import org.fabric3.api.model.type.ModelObject;

/**
 * Manages assembly templates. Templates are used in composites as a placeholder for configuration that is specified elsewhere, in another composite
 * or in system config. For example, a binding template may be used to defer configuration of environment-specific endpoint information.
 */
public interface TemplateRegistry {

    /**
     * Register the template and its parsed value.
     *
     * @param name  the template name, which must be unique in the domain
     * @param uri   the contribution the model object is contained in
     * @param value the template value
     * @throws DuplicateTemplateException if an template by the same name is already registered
     */
    <T extends ModelObject> void register(String name, URI uri, T value) throws DuplicateTemplateException;

    /**
     * Removes the template.
     *
     * @param name the template name
     */
    void unregister(String name);

    /**
     * Returns the parsed value for the template or null if the template is not registered.
     *
     * @param type the expected type of the parsed value
     * @param name the template name
     * @return the parsed value for the template or null if the template is not registered
     */
    <T extends ModelObject> T resolve(Class<T> type, String name);
}

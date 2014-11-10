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

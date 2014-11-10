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
package org.fabric3.spi.introspection.java;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Processes a {@link ComponentDefinition}, delegating to {@link ImplementationProcessor}s to add relevant metadata.
 */
public interface ComponentProcessor {

    /**
     * Processes the component definition.
     *
     * @param definition the definition
     * @param context    the introspection context to report errors
     */
    void process(ComponentDefinition<?> definition, IntrospectionContext context);

    /**
     * Processes the component definition and introspects the implementation class to create the appropriate implementation model based on annotations or
     * heuristics.
     *
     * @param definition the definition
     * @param context    the introspection context to report errors
     */
    void process(ComponentDefinition<?> definition, Class clazz, IntrospectionContext context);

}

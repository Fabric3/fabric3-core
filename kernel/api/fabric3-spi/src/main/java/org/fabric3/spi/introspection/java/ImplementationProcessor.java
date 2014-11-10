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
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Processes a {@link ComponentDefinition}, potentially adding metadata based on introspecting the component implementation.
 */
public interface ImplementationProcessor<I extends Implementation<?>> {

    /**
     * Processes the component definition.
     *
     * @param definition the component definition
     * @param context    the introspection context
     */
    void process(ComponentDefinition<I> definition, IntrospectionContext context);

    /**
     * Processes a component definition, introspecting the provided implementation class to determine the component type.
     *
     * @param definition the component definition
     * @param clazz      the implementation class
     * @param context    the introspection context
     */
    void process(ComponentDefinition<I> definition, Class<?> clazz, IntrospectionContext context);

}

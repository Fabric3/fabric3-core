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

package org.fabric3.implementation.pojo.builder;

import org.w3c.dom.Document;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Builds an object factory that creates a property value array from a DOM.
 */
public interface ArrayBuilder {

    /**
     * Creates the object factory for the property value.
     *
     * @param name        the property name
     * @param type        the property type
     * @param value       the DOM
     * @param classLoader the classloader to deserialize the property value
     * @return the object factory
     * @throws PropertyTransformException if there is an error creating the object factory
     */
    ObjectFactory<?> createFactory(String name, DataType type, Document value, ClassLoader classLoader) throws PropertyTransformException;

}
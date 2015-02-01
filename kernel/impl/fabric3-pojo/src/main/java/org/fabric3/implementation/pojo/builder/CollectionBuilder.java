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

import java.util.Collection;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.model.type.java.JavaGenericType;
import org.w3c.dom.Document;

/**
 * Builds an object factory that creates a property value collection from a DOM.
 */
public interface CollectionBuilder {

    /**
     * Creates the object factory for the property value.
     *
     * @param collection  the collection
     * @param name        the property name
     * @param type        the property type
     * @param value       the DOM
     * @param classLoader the classloader to deserialize the property value
     * @return the object factory
     * @throws Fabric3Exception if there is an error creating the object factory
     */
    <T> ObjectFactory<Collection<T>> createFactory(Collection<T> collection,
                                                   String name,
                                                   JavaGenericType type,
                                                   Document value,
                                                   ClassLoader classLoader) throws Fabric3Exception;
}
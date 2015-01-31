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

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.w3c.dom.Document;

/**
 * Creates ObjectFactory instances for property values.
 */
public interface PropertyObjectFactoryBuilder {

    /**
     * Create the ObjectFactory from the given DOM value.
     *
     * @param name        the property name
     * @param dataType    the property type
     * @param value       the DOM to transform
     * @param many        true if the property is many-valued
     * @param classLoader the classloader for the target type
     * @return the ObjectFactory
     * @throws ContainerException if there is an error building the factory
     */
    ObjectFactory<?> createFactory(String name, DataType dataType, Document value, boolean many, ClassLoader classLoader) throws ContainerException;
}

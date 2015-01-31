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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.transform;

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.host.ContainerException;

/**
 * Registry of Transformers.
 */
public interface TransformerRegistry {

    /**
     * Returns a transformer that can convert a set of classes from the source to target data type. The source and target data types may be Java types, XML
     * Schema types, etc. The in and out types represent the classes that must be converted.
     *
     * @param source   the data type to transform from
     * @param target   the data type to transform to
     * @param inTypes  the classes that must be converted from
     * @param outTypes the classes that must be converted to
     * @return the transformer or null if one is not found
     * @throws ContainerException if an error occurs returning the transformer
     */
    Transformer<?, ?> getTransformer(DataType source, DataType target, List<Class<?>> inTypes, List<Class<?>> outTypes) throws ContainerException;

}

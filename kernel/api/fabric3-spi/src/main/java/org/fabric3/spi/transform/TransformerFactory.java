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
import org.fabric3.spi.container.ContainerException;

/**
 * Creates a transformer capable of converting a set of classes from a source to target data type.
 */
public interface TransformerFactory {

    /**
     * Returns a value for ordering transformers. Higher values signify less weight;
     *
     * @return an ascending value where 0 is first
     */
    int getOrder();

    /**
     * Returns true if the factory creates transformers that can convert from the source to target data types.
     *
     * @param source the source datatype
     * @param target the target datatype
     * @return true if the factory creates transformers that can convert from the source to target data types
     */
    boolean canTransform(DataType source, DataType target);

    /**
     * Creates a transformer capable of converting from the source to target data types.
     *
     * @param source   the source data type
     * @param target   the target data type
     * @param inTypes  the physical types of the source data
     * @param outTypes the physical types of the converted data
     * @return the transformer the transformer
     * @throws ContainerException if there was an error creating the transformer
     */
    Transformer<?, ?> create(DataType source, DataType target, List<Class<?>> inTypes, List<Class<?>> outTypes) throws ContainerException;
}
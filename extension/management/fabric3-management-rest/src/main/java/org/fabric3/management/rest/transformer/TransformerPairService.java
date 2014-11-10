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
package org.fabric3.management.rest.transformer;

import java.lang.reflect.Method;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.transform.TransformationException;

/**
 * Returns a transformer pair for (de)serializing request/response types.
 */
public interface TransformerPairService {

    /**
     * Returns a transformer pair for serializing and deserializing request/response types for methods on a managed artifact.
     *
     * @param methods    the methods
     * @param inputType  the input (serialized) type
     * @param outputType the type responses should be serialized to
     * @return the pair
     * @throws TransformationException if an error occurs returning the pair
     */
    TransformerPair getTransformerPair(List<Method> methods, DataType inputType, DataType outputType) throws TransformationException;

}

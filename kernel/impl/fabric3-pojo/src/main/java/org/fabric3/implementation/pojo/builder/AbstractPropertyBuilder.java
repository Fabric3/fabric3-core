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

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
public abstract class AbstractPropertyBuilder {
    private TransformerRegistry transformerRegistry;

    public AbstractPropertyBuilder(TransformerRegistry transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
    }

    @SuppressWarnings({"unchecked"})
    protected <T> Transformer<T, ?> getTransformer(String name, DataType sourceType, DataType targetType, List<Class<?>> types) throws ContainerException {
        Transformer<T, ?> transformer = (Transformer<T, ?>) transformerRegistry.getTransformer(sourceType, targetType, types, types);
        if (transformer == null) {
            throw new ContainerException("No transformer for property " + name + " of type: " + targetType);
        }
        return transformer;
    }

}
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JsonObjectBuilderImpl implements JsonObjectBuilder {
    private static final DataType JSON_TYPE = new JavaType(String.class, "JSON");

    private TransformerRegistry transformerRegistry;

    public JsonObjectBuilderImpl(@Reference TransformerRegistry transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
    }

    @SuppressWarnings("unchecked")
    public Supplier<?> createSupplier(String name, String value, DataType dataType, ClassLoader classLoader) throws Fabric3Exception {
        Class<?> type = dataType.getType();
        if (dataType.getType().equals(String.class) || value == null) {
            // type is a string or null, return it
            return () -> value;
        }
        List<Class<?>> types = new ArrayList<>();
        types.add(type);

        Transformer transformer = transformer = transformerRegistry.getTransformer(JSON_TYPE, dataType, types, types);
        if (transformer == null) {
            throw new Fabric3Exception("No transformer for property " + name + " of type: " + type);
        }

        Object instance = transformer.transform(value, classLoader);
        return () -> instance;
    }

}
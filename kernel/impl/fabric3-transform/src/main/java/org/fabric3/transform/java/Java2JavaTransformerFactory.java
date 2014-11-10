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
package org.fabric3.transform.java;

import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * Factory for a transformer that passes Java types from one classloader to another using Java serialization.
 */
public class Java2JavaTransformerFactory implements TransformerFactory {

    public int getOrder() {
        return 10;
    }

    public boolean canTransform(DataType source, DataType target) {
        return source instanceof JavaType && target instanceof JavaType && source.getType().getName().equals(target.getType().getName());
    }

    public Transformer<?, ?> create(DataType source, DataType target, List<Class<?>> inTypes, List<Class<?>> outTypes) {
        return new Java2JavaTransformer();
    }
}
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

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class TransformerPairServiceImpl implements TransformerPairService {
    private static final JavaType JAVA_TYPE = new JavaType(Object.class);

    private TransformerRegistry registry;

    public TransformerPairServiceImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public TransformerPair getTransformerPair(List<Method> methods, DataType inputType, DataType outputType) throws TransformationException {
        List<Class<?>> list = new ArrayList<>();
        JavaType type;
        if (methods.size() == 1) {
            Method method = methods.get(0);
            if (Void.TYPE.equals(method.getReturnType()) && method.getParameterTypes().length == 1) {
                type = new JavaType(method.getParameterTypes()[0]);
            } else {
                type = JAVA_TYPE;
            }
        } else {
            for (Method method : methods) {
                list.addAll(Arrays.asList(method.getParameterTypes()));
                list.addAll(Arrays.asList(method.getExceptionTypes()));
                list.add(method.getReturnType());
            }
            type = JAVA_TYPE;
        }
        Transformer<InputStream, Object> deserializer =
                (Transformer<InputStream, Object>) registry.getTransformer(inputType, type, list, list);
        Transformer<Object, byte[]> serializer = (Transformer<Object, byte[]>) registry.getTransformer(type, outputType, list, list);
        return new TransformerPair(deserializer, serializer);
    }

}

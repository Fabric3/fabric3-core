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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.pojo.builder;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.implementation.pojo.provision.PojoWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.model.type.TypeConstants;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.ClassLoading;

/**
 * Contains functionality common to Java-based SourceWireAttachers.
 */
public abstract class PojoSourceWireAttacher {

    protected TransformerRegistry transformerRegistry;

    protected PojoSourceWireAttacher(TransformerRegistry transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
    }

    /**
     * Returns a key for a map-style reference or null if there is no key associated with the reference.
     *
     * @param source the source metadata
     * @param target the target metadata
     * @return the key
     * @throws Fabric3Exception if there is an error instantiating the key
     */
    @SuppressWarnings("unchecked")
    protected Object getKey(PojoWireSource source, PhysicalWireTarget target) throws Fabric3Exception {
        if (!source.isKeyed()) {
            return null;
        }
        String key = source.getKey();

        // The target classloader must be used since the key class may not be visible to the source classloader, for example, when subclasses are used as keys
        ClassLoader targetClassLoader = target.getClassLoader();

        Class<?> keyType = ClassLoading.loadClass(targetClassLoader, source.getKeyClassName());
        if (String.class.equals(keyType)) {
            // short-circuit the transformation and return the string
            return key;
        } else if (Enum.class.isAssignableFrom(keyType)) {
            // enum, instantiate it directly
            Class<Enum> enumClass = (Class<Enum>) keyType;
            return Enum.valueOf(enumClass, key);
        }
        JavaType targetType = new JavaType(keyType);
        return createKey(targetType, key, targetClassLoader);

    }

    @SuppressWarnings("unchecked")
    private Object createKey(DataType targetType, String value, ClassLoader classLoader) throws Fabric3Exception {
        Class<?> type = targetType.getType();
        List<Class<?>> types = new ArrayList<>();
        types.add(type);
        Transformer<String, ?> transformer = (Transformer<String, ?>) transformerRegistry.getTransformer(TypeConstants.STRING_TYPE, targetType, types, types);
        if (transformer == null) {
            throw new Fabric3Exception("No transformer for : " + targetType);
        }
        return transformer.transform(value, classLoader);
    }

}

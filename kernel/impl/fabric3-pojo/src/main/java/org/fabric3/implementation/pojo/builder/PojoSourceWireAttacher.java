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

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.implementation.pojo.provision.PojoWireSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * Contains functionality common to Java-based SourceWireAttachers.
 */
public abstract class PojoSourceWireAttacher {
    private static final XSDSimpleType STRING_TYPE = new XSDSimpleType(String.class, new QName(XSDType.XSD_NS, "string"));

    protected TransformerRegistry transformerRegistry;
    protected ClassLoaderRegistry classLoaderRegistry;

    protected PojoSourceWireAttacher(TransformerRegistry transformerRegistry, ClassLoaderRegistry loaderRegistry) {
        this.transformerRegistry = transformerRegistry;
        this.classLoaderRegistry = loaderRegistry;
    }

    /**
     * Returns a key for a map-style reference or null if there is no key associated with the reference.
     *
     * @param sourceDefinition the source metadata
     * @param targetDefinition the target metadata
     * @return the key
     * @throws KeyInstantiationException if there is an error instantiating the key
     */
    @SuppressWarnings("unchecked")
    protected Object getKey(PojoWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition) throws KeyInstantiationException {
        if (!sourceDefinition.isKeyed()) {
            return null;
        }
        String key = sourceDefinition.getKey();

        // The target classloader must be used since the key class may not be visible to the source classloader, for example, when subclasses are
        // used a keys
        URI targetId = targetDefinition.getClassLoaderId();
        ClassLoader targetClassLoader = classLoaderRegistry.getClassLoader(targetId);

        Class<?> keyType;
        try {
            keyType = classLoaderRegistry.loadClass(targetClassLoader, sourceDefinition.getKeyClassName());
        } catch (ClassNotFoundException e) {
            throw new KeyInstantiationException("Error loading reference key type for: " + sourceDefinition.getUri(), e);
        }
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
    private Object createKey(DataType targetType, String value, ClassLoader classLoader) throws KeyInstantiationException {
        try {
            Class<?> type = targetType.getType();
            List<Class<?>> types = new ArrayList<>();
            types.add(type);
            Transformer<String, ?> transformer = (Transformer<String, ?>) transformerRegistry.getTransformer(STRING_TYPE, targetType, types, types);
            if (transformer == null) {
                throw new KeyInstantiationException("No transformer for : " + targetType);
            }
            return transformer.transform(value, classLoader);
        } catch (TransformationException e) {
            throw new KeyInstantiationException("Error transforming property", e);
        }
    }

}

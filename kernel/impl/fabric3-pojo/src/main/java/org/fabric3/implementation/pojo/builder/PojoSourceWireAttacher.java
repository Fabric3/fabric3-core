/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.builder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.implementation.pojo.provision.PojoSourceDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.JavaClass;
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
    protected Object getKey(PojoSourceDefinition sourceDefinition, PhysicalTargetDefinition targetDefinition) throws KeyInstantiationException {
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
        DataType<?> targetType = new JavaClass(keyType);
        return createKey(targetType, key, targetClassLoader);

    }


    @SuppressWarnings("unchecked")
    private Object createKey(DataType<?> targetType, String value, ClassLoader classLoader) throws KeyInstantiationException {
        try {
            Class<?> physical = targetType.getPhysical();
            List<Class<?>> types = new ArrayList<Class<?>>();
            types.add(physical);
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

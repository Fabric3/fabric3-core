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
package org.fabric3.spi.introspection;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

/**
 * A mapping from generic formal types to actual types for a class hierarchy.
 *
 * @version $Rev$ $Date$
 */
public class TypeMapping {
    private final Map<? super Type, Type> mappings = new HashMap<Type, Type>();

    /**
     * Add a mapping from a TypeVariable to an actual type.
     *
     * @param typeVariable the formal type variable
     * @param type         the actual type it maps to
     */
    public void addMapping(TypeVariable<?> typeVariable, Type type) {
        mappings.put(typeVariable, type);
    }

    /**
     * Return the actual type of the supplied formal type. That is, the Class, GenericArrayType, TypeVariable or WildCardType that the formal type is
     * bound to.
     *
     * @param type the formal type parameter
     * @return the actual type; may be a TypeVariable if the type is not bound
     */
    public Type getActualType(Type type) {
        while (true) {
            Type actual = mappings.get(type);
            if (actual == null) {
                return type;
            } else {
                type = actual;
            }
        }
    }

    /**
     * Return the raw type of the supplied formal type.
     *
     * @param type the formal type parameter
     * @return the actual class for that parameter
     */
    public Class<?> getRawType(Type type) {
        Type actualType = getActualType(type);
        if (actualType instanceof Class<?>) {
            return (Class<?>) actualType;
        } else if (actualType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) actualType;
            return getRawType(typeVariable.getBounds()[0]);
        } else if (actualType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) actualType;
            return (Class<?>) parameterizedType.getRawType();
        } else if (actualType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) actualType;
            Class<?> componentType = getRawType(arrayType.getGenericComponentType());
            return Array.newInstance(componentType, 0).getClass();
        } else if (actualType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) actualType;
            Type[] bounds = wildcardType.getUpperBounds();
            return getRawType(bounds[0]);
        } else {
            throw new AssertionError();
        }
    }

}
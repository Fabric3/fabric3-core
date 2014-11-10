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
package org.fabric3.spi.model.physical;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for loading operation parameter and fault types.
 */
public class ParameterTypeHelper {
    private ParameterTypeHelper() {
    }

    /**
     * Loads input parameter types for the source side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static List<Class<?>> loadSourceInParameterTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<>();
        for (String param : operation.getSourceParameterTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads input parameter types for the target side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static List<Class<?>> loadTargetInParameterTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<>();
        for (String param : operation.getTargetParameterTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads declared fault parameter types for the source side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static List<Class<?>> loadSourceFaultTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<>();
        for (String param : operation.getSourceFaultTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads declared fault parameter types for the target side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static List<Class<?>> loadTargetFaultTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<>();
        for (String param : operation.getTargetFaultTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads the output parameter type for the source side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static Class<?> loadSourceOutputType(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        // currently only one type is supported although WSDL allows multiple
        return loadClass(operation.getSourceReturnType(), loader);
    }

    /**
     * Loads the output parameter type for the target side of an operation.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws ClassNotFoundException if an error occurs loading the types
     */
    public static Class<?> loadTargetOutputType(PhysicalOperationDefinition operation, ClassLoader loader) throws ClassNotFoundException {
        // currently only one type is supported although WSDL allows multiple
        return loadClass(operation.getTargetReturnType(), loader);
    }

    /**
     * Loads a class identified by the given name. Primitives are also handled.
     *
     * @param name   the class name
     * @param loader the classloader to use for loading
     * @return the class
     * @throws ClassNotFoundException if an error occurs loading the class
     */
    public static Class<?> loadClass(String name, ClassLoader loader) throws ClassNotFoundException {
        Class<?> clazz;
        clazz = ParamTypes.PRIMITIVES_TYPES.get(name);
        if (clazz == null) {
            clazz = loader.loadClass(name);
        }
        return clazz;
    }
}
/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.spi.model.physical;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.util.ParamTypes;

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
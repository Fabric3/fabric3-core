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
package org.fabric3.node;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.fabric3.api.node.FabricException;

/**
 * ClassLoader utilities
 */
public class ClassLoaderUtils {

    /**
     * Changes the classloader parent to the new one.
     *
     * @param classLoader the classloader
     * @param newParent   the new parent
     */
    public static void changeParentClassLoader(ClassLoader classLoader, ClassLoader newParent) {
        try {
            // get the parent classloader field
            Field parentField = getParentClassLoaderField();
            parentField.setAccessible(true);

            // change the final modifier
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(parentField, parentField.getModifiers() & ~Modifier.FINAL);

            // set the parent classloader field to the new value
            parentField.set(classLoader, newParent);
        } catch (IllegalAccessException e) {
            throw new FabricException("Unsupported VM", e);
        } catch (NoSuchFieldException e) {
            throw new FabricException("Unsupported VM", e);
        }
    }

    private static Field getParentClassLoaderField() {
        Field parentField;
        try {
            // try the Sun implementation first
            parentField = ClassLoader.class.getDeclaredField("parent");
        } catch (NoSuchFieldException e) {
            // not found, try J9
            try {
                parentField = ClassLoader.class.getDeclaredField("parentClassLoader");
            } catch (NoSuchFieldException e1) {
                throw new FabricException("Unsupported VM", e);
            }
        }
        return parentField;
    }

    private ClassLoaderUtils() {
    }
}

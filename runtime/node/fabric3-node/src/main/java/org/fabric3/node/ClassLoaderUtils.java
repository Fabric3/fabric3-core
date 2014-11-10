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
    	if ("dalvik".equalsIgnoreCase(System.getProperty("java.vm.name"))){
    		// android , not change
    		return;
    	}
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
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

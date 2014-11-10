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
package org.fabric3.binding.ws.metro.util;

import java.lang.reflect.InvocationTargetException;
import java.security.SecureClassLoader;

/**
 * Defines a class using a classloader.
 */
public interface ClassDefiner {

    /**
     * Define the class.
     *
     * @param name   the class name
     * @param bytes  the class bytes
     * @param loader the classloader to use
     * @return the loaded class
     * @throws IllegalAccessException    if there is an error defining the class
     * @throws InvocationTargetException if there is an error defining the class
     */
    Class<?> defineClass(String name, byte[] bytes, SecureClassLoader loader) throws IllegalAccessException, InvocationTargetException;

}

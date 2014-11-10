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
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.SecureClassLoader;

import org.oasisopen.sca.annotation.Init;

/**
 *
 */
public class ClassDefinerImpl implements ClassDefiner {
    private Method method;

    @Init
    public void init() throws NoSuchMethodException {
        method = SecureClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE, CodeSource.class);
        method.setAccessible(true);
    }

    public Class<?> defineClass(String name, byte[] bytes, SecureClassLoader loader) throws IllegalAccessException, InvocationTargetException {
        try {
            // check if the class was already generated
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return (Class<?>) method.invoke(loader, name, bytes, 0, bytes.length, getClass().getProtectionDomain().getCodeSource());
    }

}

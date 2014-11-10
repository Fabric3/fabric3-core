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
package org.fabric3.fabric.classloader;

import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Implementation of a registry for classloaders.
 */
public class ClassLoaderRegistryImpl implements ClassLoaderRegistry {
    private final Map<URI, ClassLoader> registry = new ConcurrentHashMap<>();
    private static final Map<String, Class<?>> PRIMITIVES;

    static {
        PRIMITIVES = new HashMap<>();
        PRIMITIVES.put("boolean", Boolean.TYPE);
        PRIMITIVES.put("byte", Byte.TYPE);
        PRIMITIVES.put("short", Short.TYPE);
        PRIMITIVES.put("int", Integer.TYPE);
        PRIMITIVES.put("long", Long.TYPE);
        PRIMITIVES.put("float", Float.TYPE);
        PRIMITIVES.put("double", Double.TYPE);
        PRIMITIVES.put("void", Void.TYPE);
    }

    public synchronized void register(URI id, ClassLoader classLoader) {
        if (registry.containsKey(id)) {
            throw new AssertionError("Duplicate class loader: " + id);
        }
        registry.put(id, classLoader);
    }

    public ClassLoader unregister(URI id) {
        return registry.remove(id);
    }

    public ClassLoader getClassLoader(URI id) {
        return registry.get(id);
    }

    public Map<URI, ClassLoader> getClassLoaders() {
        return registry;
    }

    public Class<?> loadClass(URI classLoaderId, String className) throws ClassNotFoundException {
        ClassLoader cl = getClassLoader(classLoaderId);
        return loadClass(cl, className);
    }

    public Class<?> loadClass(ClassLoader cl, String className) throws ClassNotFoundException {
        Class<?> clazz = PRIMITIVES.get(className);
        if (clazz == null) {
            clazz = Class.forName(className, true, cl);
        }
        return clazz;
    }

    @Override
    public void close() throws IOException {
        if (registry == null || registry.isEmpty()) {
            return;
        }
        Collection<ClassLoader> classLoaders = registry.values();
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader instanceof URLClassLoader) {
                ((URLClassLoader) classLoader).close();
            }
        }
    }
}

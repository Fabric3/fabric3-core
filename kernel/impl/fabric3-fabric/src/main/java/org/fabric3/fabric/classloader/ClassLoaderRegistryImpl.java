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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Implementation of a registry for classloaders.
 */
public class ClassLoaderRegistryImpl implements ClassLoaderRegistry {
    private Map<URI, ClassLoader> registry = new ConcurrentHashMap<>();

    public synchronized void register(URI id, ClassLoader classLoader) {
        if (registry.containsKey(id)) {
            throw new Fabric3Exception("Duplicate classloader: " + id);
        }
        registry.put(id, classLoader);
    }

    public ClassLoader unregister(URI id) {
        return registry.remove(id);
    }

    public ClassLoader getClassLoader(URI id) {
        return registry.get(id);
    }

    public void close() {
        if (registry == null || registry.isEmpty()) {
            return;
        }
        Collection<ClassLoader> classLoaders = registry.values();
        for (ClassLoader classLoader : classLoaders) {
            if (classLoader instanceof URLClassLoader) {
                try {
                    ((URLClassLoader) classLoader).close();
                } catch (IOException e) {
                    throw new Fabric3Exception(e);
                }
            }
        }
    }
}

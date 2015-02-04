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
package org.fabric3.spi.classloader;

import java.net.URI;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Registry for classloaders available to the local runtime.
 */
public interface ClassLoaderRegistry {
    /**
     * Register a ClassLoader with the runtime.
     *
     * @param id          a unique id for the classloader
     * @param classLoader the classloader to register
     */
    void register(URI id, ClassLoader classLoader);

    /**
     * Unregister the specified classloader from the system.
     *
     * @param id the id for the classloader
     * @return the classloader that was registered with the id, or null if none
     */
    ClassLoader unregister(URI id);

    /**
     * Returns the classloader registered with the supplied id, or null if none is registered.
     *
     * @param id the id for the classloader
     * @return the ClassLoader registered with that id, or null
     */
    ClassLoader getClassLoader(URI id);

    /**
     * Returns all registered classloaders.
     *
     * @return all registered classloaders
     */
    Map<URI, ClassLoader> getClassLoaders();

    /**
     * Load and define a class from a specific classloader.
     *
     * @param cl        the ClassLoader to use
     * @param className the name of the class
     * @return the class
     * @throws Fabric3Exception if the class could not be found by that classloader
     */
    Class<?> loadClass(ClassLoader cl, String className) throws Fabric3Exception;

    /**
     * Closes all classloaders.
     */
    void close();
}

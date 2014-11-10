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
package org.fabric3.fabric.container.builder.classloader;

import java.net.URI;

/**
 * Tracks classloader usage across deployments on a participant runtime.
 * <p/>
 * SCA's import/export mechanism allows contribution classloaders to be referenced by multiple deployments (e.g. a contribution with deployed
 * components may import another contribution that exports an API package). This service tracks usage, signaling when a contribution classloader is no
 * longer referenced and may be removed.
 */
public interface ClassLoaderTracker {

    /**
     * Returns true if the contribution classloader is in use.
     *
     * @param uri the contribution classloader id
     * @return true if the contribution classloader is in use; otherwise false
     */
    boolean isReferenced(URI uri);

    /**
     * Increments the use count for a contribution classloader.
     *
     * @param uri the contribution classloader id
     * @return the count after it is incremented
     */
    int increment(URI uri);

    /**
     * Increments the use count of all transitively imported classloaders starting with the given (parent) classloader.
     *
     * @param classLoader the imported classloader, which is generally a parent of the contribution classloader
     */
    void incrementImported(ClassLoader classLoader);

    /**
     * Decrements the in use count for a contribution classloader and all transitively imported contribution classloaders.
     *
     * @param classLoader the contribution classloader
     * @return the count after it is decremented
     */
    int decrement(ClassLoader classLoader);

}
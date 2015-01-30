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
package org.fabric3.spi.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.spi.container.ContainerException;

/**
 * Temporarily stores artifacts locally to a runtime.
 */
public interface ArtifactCache {

    /**
     * Temporarily persists an artifact. The lifetime of the artifact will not extend past the lifetime of the runtime instance. When the operation
     * completes, the input stream will be closed.
     *
     * @param uri    The artifact URI
     * @param stream the artifact contents
     * @return a URL for the persisted artifact
     * @throws ContainerException if an error occurs persisting the artifact
     */
    URL cache(URI uri, InputStream stream) throws ContainerException;

    /**
     * Returns the URL for the cached artifact or null if not found.
     *
     * @param uri the artifact URI
     * @return the URL for the cached artifact or null if not found
     */
    URL get(URI uri);

    /**
     * Evicts an artifact.
     *
     * @param uri the artifact URI.
     * @return returns true if the artifact was evicted
     * @throws ContainerException if an error occurs releasing the artifact
     */
    boolean remove(URI uri) throws ContainerException;

}

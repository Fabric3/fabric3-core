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
package org.fabric3.api.host.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Implementations store and retrieve artifacts such as contributions from persistent storage.
 */
public interface Repository {

    /**
     * Persists a user artifact to the repository.
     *
     * @param uri    The artifact URI
     * @param stream the artifact contents
     * @param extension true if the artifact is a runtime extension
     * @return a URL for the persisted artifact
     * @throws Fabric3Exception if an error occurs storing the artifact
     */
    URL store(URI uri, InputStream stream, boolean extension) throws Fabric3Exception;

    /**
     * Returns true if the artifact exists.
     *
     * @param uri the artifact URI
     * @return true if the archive exists
     */
    boolean exists(URI uri);

    /**
     * Look up the artifact URL by URI.
     *
     * @param uri The artifact URI
     * @return A URL pointing to the artifact or null if the artifact cannot be found
     * @throws Fabric3Exception if an exception occurs finding the artifact
     */
    URL find(URI uri) throws Fabric3Exception;

    /**
     * Removes an artifact from the repository.
     *
     * @param uri The URI of the artifact to be removed
     * @throws Fabric3Exception if an exception occurs removing the artifact
     */
    void remove(URI uri) throws Fabric3Exception;

    /**
     * Returns a list of URIs for all the artifacts in the repository.
     *
     * @return A list of artifact URIs
     */
    List<URI> list();

    /**
     * Callback to signal for the repository it can close open resources.
     *
     * @throws Fabric3Exception if an error shutting down occurs.
     */
    void shutdown() throws Fabric3Exception;
}

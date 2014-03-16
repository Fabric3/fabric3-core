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
package org.fabric3.api.host.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

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
     * @throws RepositoryException if an error occurs storing the artifact
     */
    URL store(URI uri, InputStream stream, boolean extension) throws RepositoryException;

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
     * @throws RepositoryException if an exception occurs finding the artifact
     */
    URL find(URI uri) throws RepositoryException;

    /**
     * Removes an artifact from the repository.
     *
     * @param uri The URI of the artifact to be removed
     * @throws RepositoryException if an exception occurs removing the artifact
     */
    void remove(URI uri) throws RepositoryException;

    /**
     * Returns a list of URIs for all the artifacts in the repository.
     *
     * @return A list of artifact URIs
     */
    List<URI> list();

    /**
     * Callback to signal for the repository it can close open resources.
     *
     * @throws RepositoryException if an error shutting down occurs.
     */
    void shutdown() throws RepositoryException;
}

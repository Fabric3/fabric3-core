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
package org.fabric3.api.host.contribution;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Manages artifacts contributed to a domain. Contributions can be application or extension artifacts. Contributions may be in a variety of formats, for
 * example, a JAR or XML document.  The lifecycle of a contribution is defined as follows:
 * <pre>
 * <ul>
 * <li>Stored - The contribution artifact is persisted.
 * <li>Installed - The contribution is introspected, validated, and loaded.
 * <li>Uninstalled - The contribution is unloaded.
 * <li>Removed - the contribution is removed from persistent storage.
 * </ul>
 */
public interface ContributionService {

    /**
     * Returns the URIs of contributions in the domain.
     *
     * @return the URIs of contributions in the domain
     */
    Set<URI> getContributions();

    /**
     * Returns true if a contribution for the given URI exists.
     *
     * @param uri the contribution URI
     * @return true if a contribution for the given URI exists
     */
    boolean exists(URI uri);

    /**
     * Returns a list of deployables in a contribution.
     *
     * @param uri the URI of the contribution to search
     * @return a list of deployables in a contribution. If no deployables are found, an empty list is returned.
     * @throws Fabric3Exception if a contribution corresponding to the URI is not found
     */
    List<Deployable> getDeployables(URI uri) throws Fabric3Exception;

    /**
     * Persistently stores a contribution in the domain.
     *
     * @param source the contribution source
     * @return a URI that uniquely identifies this contribution within the domain
     * @throws Fabric3Exception if there is an error storing the contribution
     */
    URI store(ContributionSource source) throws Fabric3Exception;

    /**
     * Persistently stores a collection of contributions in the domain.
     *
     * @param sources the contribution sources
     * @return URIs that uniquely identify the contribution within the domain
     * @throws Fabric3Exception if there is an error storing the contribution
     */
    List<URI> store(List<ContributionSource> sources) throws Fabric3Exception;

    /**
     * Installs a stored contribution.
     *
     * @param uri the contribution URI
     * @throws Fabric3Exception if there an error reading, introspecting or loading the contribution
     */
    void install(URI uri) throws Fabric3Exception;

    /**
     * Installs a list of stored contributions.
     *
     * @param uris the contribution URIs
     * @return the list of installed URIs ordered by dependencies
     * @throws Fabric3Exception if there an error reading, introspecting or loading the contribution
     */
    List<URI> install(List<URI> uris) throws Fabric3Exception;

    /**
     * Persistently stores a collection of contributions and processes their manifests.
     *
     * @param sources the contribution sources
     * @return metadata representing the dependency ordering of the contributions
     * @throws Fabric3Exception if there is an error storing the contribution
     */
    ContributionOrder processManifests(List<ContributionSource> sources) throws Fabric3Exception;

    /**
     * Introspects the contents of a contribution.
     *
     * @param uri the contribution URI
     * @throws Fabric3Exception if there is an error
     */
    void processContents(URI uri) throws Fabric3Exception;

    /**
     * Uninstalls a contribution.
     *
     * @param uri The URI of the contribution
     * @throws Fabric3Exception if there was a problem with the contribution
     */
    void uninstall(URI uri) throws Fabric3Exception;

    /**
     * Uninstalls multiple contributions.
     *
     * @param uris The URIs of the contributions
     * @throws Fabric3Exception if there was a problem with the contribution
     */
    void uninstall(List<URI> uris) throws Fabric3Exception;

    /**
     * Remove a contribution from persistent storage. Contribution must be uninstalled prior to being removed.
     *
     * @param uri The URI of the contribution
     * @throws Fabric3Exception if there was a problem with the contribution
     */
    void remove(URI uri) throws Fabric3Exception;

    /**
     * Remove multiple contributions from persistent storage. Contribution must be uninstalled prior to being removed.
     *
     * @param uris The URIs of the contributions
     * @throws Fabric3Exception if there was a problem with the contribution
     */
    void remove(List<URI> uris) throws Fabric3Exception;

}

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
import javax.xml.namespace.QName;


/**
 * Manages artifacts contributed to a domain. Contributions can be application or extension artifacts. Contributions may be in a variety of formats,
 * for example, a JAR or XML document.
 * <p/>
 * The lifecycle of a contribution is defined as follows:
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
     * Returns the contribution timestamp.
     *
     * @param uri the contribution URI
     * @return the timestamp or -1 if no contribution was found
     */
    long getContributionTimestamp(URI uri);

    /**
     * Returns a list of deployables in a contribution.
     *
     * @param uri the URI of the contribution to search
     * @return a list of deployables in a contribution. If no deployables are found, an empty list is returned.
     * @throws ContributionNotFoundException if a contribution corresponding to the URI is not found
     */
    List<Deployable> getDeployables(URI uri) throws ContributionNotFoundException;

    /**
     * Returns a list of deployed deployable composites from the contribution in the order they were deployed.
     *
     * @param uri the contribution URI
     * @return the list of deployed composite qualified names
     * @throws ContributionNotFoundException if the contribution is not found
     */
    List<QName> getDeployedComposites(URI uri) throws ContributionNotFoundException;

    /**
     * Persistently stores a contribution in the domain.
     *
     * @param source the contribution source
     * @return a URI that uniquely identifies this contribution within the domain
     * @throws StoreException if there is an error storing the contribution
     */
    URI store(ContributionSource source) throws StoreException;

    /**
     * Persistently stores a collection of contributions in the domain.
     *
     * @param sources the contribution sources
     * @return URIs that uniquely identify the contribution within the domain
     * @throws StoreException if there is an error storing the contribution
     */
    List<URI> store(List<ContributionSource> sources) throws StoreException;

    /**
     * Installs a stored contribution.
     *
     * @param uri the contribution URI
     * @throws InstallException              if there an error reading, introspecting or loading the contribution
     * @throws ContributionNotFoundException if the contribution is not found
     */
    void install(URI uri) throws InstallException, ContributionNotFoundException;

    /**
     * Installs a list of stored contributions.
     *
     * @param uris the contribution URIs
     * @return the list of installed URIs ordered by dependencies
     * @throws InstallException              if there an error reading, introspecting or loading the contribution
     * @throws ContributionNotFoundException if a contribution is not found
     */
    List<URI> install(List<URI> uris) throws InstallException, ContributionNotFoundException;

    /**
     * Persistently stores a collection of contributions and processes their manifests.
     *
     * @param sources the contribution sources
     * @return metadata representing the dependency ordering of the contributions
     * @throws StoreException   if there is an error storing the contribution
     * @throws InstallException if there an error reading, introspecting or loading the contribution
     */
    ContributionOrder processManifests(List<ContributionSource> sources) throws StoreException, InstallException;

    /**
     * Introspects the contents of a contribution.
     *
     * @param uri the contribution URI
     * @throws InstallException              if there is an error introspecting the contribution
     * @throws ContributionNotFoundException if the contribution is not found
     */
    void processContents(URI uri) throws InstallException, ContributionNotFoundException;

    /**
     * Uninstalls a contribution.
     *
     * @param uri The URI of the contribution
     * @throws UninstallException            if there was a problem with the contribution
     * @throws ContributionNotFoundException if a contribution is not found
     */
    void uninstall(URI uri) throws UninstallException, ContributionNotFoundException;

    /**
     * Uninstalls multiple contributions.
     *
     * @param uris The URIs of the contributions
     * @throws UninstallException            if there was a problem with the contribution
     * @throws ContributionNotFoundException if a contribution is not found
     */
    void uninstall(List<URI> uris) throws UninstallException, ContributionNotFoundException;

    /**
     * Remove a contribution from persistent storage. Contribution must be uninstalled prior to being removed.
     *
     * @param uri The URI of the contribution
     * @throws RemoveException               if there was a problem with the contribution
     * @throws ContributionNotFoundException if a contribution is not found
     */
    void remove(URI uri) throws RemoveException, ContributionNotFoundException;

    /**
     * Remove multiple contributions from persistent storage. Contribution must be uninstalled prior to being removed.
     *
     * @param uris The URIs of the contributions
     * @throws RemoveException               if there was a problem with the contribution
     * @throws ContributionNotFoundException if a contribution is not found
     */
    void remove(List<URI> uris) throws ContributionNotFoundException, RemoveException;

    /**
     * Returns true if the profile exists.
     *
     * @param uri the profile URI
     * @return true if the profile exists
     */
    boolean profileExists(URI uri);

    /**
     * Returns a list of contributions contained in a profile.
     *
     * @param uri the profile URI
     * @return the list of contributions contained in the profile
     */
    List<URI> getContributionsInProfile(URI uri);

    /**
     * Returns a list of contributions contained in a profile sorted topologically by dependencies.
     *
     * @param uri the profile URI
     * @return the list of contributions contained in the profile
     */
    List<URI> getSortedContributionsInProfile(URI uri);

    /**
     * Registers a profile.
     *
     * @param profileUri       the profile URI
     * @param contributionUris the URIs of contributions that are contained in the profile
     * @throws DuplicateProfileException if a profile already exists by that name
     */
    void registerProfile(URI profileUri, List<URI> contributionUris) throws DuplicateProfileException;

    /**
     * Installs a profile. This operation involves installing contributions contained in the profile that have not been previously installed.
     *
     * @param uri the profile URI.
     * @throws InstallException              if there was an error installing the profile
     * @throws ContributionNotFoundException if a contribution was not found;
     */
    void installProfile(URI uri) throws InstallException, ContributionNotFoundException;

    /**
     * Uninstalls a profile. This operation involves uninstalling contributions contained in the profile that are not members of another installed
     * profile.
     *
     * @param uri the profile URI.
     * @throws UninstallException            if there was an error uninstalling the profile
     * @throws ContributionNotFoundException if a contribution was not found;
     */
    void uninstallProfile(URI uri) throws UninstallException, ContributionNotFoundException;

    /**
     * Removes a profile. This operation involves removing contributions contained in the profile that are not members of another stored or installed
     * profile.
     *
     * @param uri the profile URI.
     * @throws RemoveException               if there was an error uninstalling the profile
     * @throws ContributionNotFoundException if a contribution was not found;
     */
    void removeProfile(URI uri) throws RemoveException, ContributionNotFoundException;

}

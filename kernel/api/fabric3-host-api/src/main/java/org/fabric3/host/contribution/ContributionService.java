/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host.contribution;

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
 *
 * @version $Rev$ $Date$
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

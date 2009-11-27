/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.admin.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.fabric3.management.contribution.ContributionInfo;
import org.fabric3.management.contribution.ContributionInstallException;
import org.fabric3.management.contribution.ContributionManagementException;
import org.fabric3.management.contribution.ContributionRemoveException;
import org.fabric3.management.contribution.ContributionUninstallException;
import org.fabric3.management.domain.ComponentInfo;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.management.domain.InvalidPathException;

/**
 * The interface for performing domain administrative functions.
 *
 * @version $Rev$ $Date$
 */
public interface DomainController {

    /**
     * Sets the base domain admin address.
     *
     * @param address the domain admin address
     */
    void setDomainAddress(String address);

    /**
     * Sets the username to authenticate with.
     *
     * @param username a valid domain admin username
     */
    void setUsername(String username);

    /**
     * Sets the password to authenticate with.
     *
     * @param password a valid domain admin password
     */
    void setPassword(String password);

    /**
     * Returns true if a connection to the domain controller is open.
     *
     * @return true if a connection to the domain controller is open
     */
    boolean isConnected();

    /**
     * Open a connection to the domain controller.
     *
     * @throws IOException if a connection cannot be established
     */
    void connect() throws IOException;

    /**
     * Closes an open connection to the domain controller.
     *
     * @throws IOException if there is an error closing the connection.
     */
    void disconnect() throws IOException;

    /**
     * Returns a set of installed contributions in the domain.
     *
     * @return the set of installed contributions.
     * @throws CommunicationException if there is an error communicating with the domain controller
     */
    public Set<ContributionInfo> stat() throws CommunicationException;


    /**
     * Stores a contribution in the domain.
     *
     * @param contribution a URL pointing to the contribution artifact
     * @param uri          the URI to assign the contribution.
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws ContributionManagementException
     *                                if there is an error storing the contribution.
     */
    void store(URL contribution, URI uri) throws CommunicationException, ContributionManagementException;

    /**
     * Installs a contribution.
     *
     * @param uri the URI to assign the contribution.
     * @throws CommunicationException       if there is an error communicating with the domain controller
     * @throws ContributionInstallException if there is an error installing the contribution. See InstallException subtypes for specific errors that
     *                                      may be thrown.
     */
    void install(URI uri) throws CommunicationException, ContributionInstallException;

    /**
     * Deploys all deployables in a contribution.
     *
     * @param uri the contribution uri.
     * @throws CommunicationException        if there is an error communicating with the domain controller
     * @throws DeploymentManagementException if there is an error deploying the contribution. See InstallException subtypes for specific errors that
     *                                       may be thrown.
     */
    void deploy(URI uri) throws CommunicationException, DeploymentManagementException;

    /**
     * Deploys all deployables in a contribution.
     *
     * @param uri  the contribution URI.
     * @param plan the name of the deployment plan
     * @throws CommunicationException        if there is an error communicating with the domain controller
     * @throws DeploymentManagementException if there is an error deploying the contribution. See InstallException subtypes for specific errors that
     *                                       may be thrown.
     */
    void deploy(URI uri, String plan) throws CommunicationException, DeploymentManagementException;

    /**
     * Undeploys all deployables in a contribution.
     *
     * @param uri the contribution URI.
     * @throws CommunicationException        if there is an error communicating with the domain controller
     * @throws DeploymentManagementException if there is an error undeploying the contribution.
     */
    void undeploy(URI uri) throws CommunicationException, DeploymentManagementException;

    /**
     * Uninstalls a contribution.
     *
     * @param uri the contribution URI
     * @throws CommunicationException         if there is an error communicating with the domain controller
     * @throws ContributionUninstallException if the is an error uninstalling the contribution
     */
    void uninstall(URI uri) throws CommunicationException, ContributionUninstallException;

    /**
     * Removes a contribution from storage in a domain.
     *
     * @param uri the contribution URI
     * @throws CommunicationException      if there is an error communicating with the domain controller
     * @throws ContributionRemoveException if the is an error removing the contribution
     */
    void remove(URI uri) throws CommunicationException, ContributionRemoveException;

    void storeProfile(URL profile, URI uri) throws CommunicationException, ContributionManagementException;

    void installProfile(URI uri) throws CommunicationException, ContributionInstallException;

    void uninstallProfile(URI uri) throws CommunicationException, ContributionUninstallException;

    void removeProfile(URI uri) throws CommunicationException, ContributionRemoveException;


    /**
     * Returns a list of ComponentInfo instances representing the components deployed to the given composite path. The path "/" is interpreted as the
     * domain composite.
     *
     * @param path the composite
     * @return the list of deployed components for the composite
     * @throws CommunicationException if there is an error communicating with the domain controller
     * @throws InvalidPathException   if the composite path is invalid
     */
    List<ComponentInfo> getDeployedComponents(String path) throws CommunicationException, InvalidPathException;
}

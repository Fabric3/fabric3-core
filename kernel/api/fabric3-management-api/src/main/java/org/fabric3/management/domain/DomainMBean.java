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
package org.fabric3.management.domain;

import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.management.Management;

/**
 * MBean for invoking domain operations.
 *
 * @version $Rev$ $Date$
 */
@Management
public interface DomainMBean {

    /**
     * Deploys a contribution to the domain.  All contained deployables will be included in the domain composite.
     *
     * @param uri the contribution URI.
     * @throws DeploymentManagementException if an exception deploying the contribution is encountered
     */
    void deploy(URI uri) throws DeploymentManagementException;


    /**
     * Deploys a contribution to the domain using the specified deployment plan.  All contained deployables will be included in the domain composite.
     *
     * @param uri  the contribution URI.
     * @param plan the deployment plan name
     * @throws DeploymentManagementException if an exception deploying the contribution is encountered
     */
    void deploy(URI uri, String plan) throws DeploymentManagementException;

    /**
     * Undeploys deployables contained in a contribution.
     *
     * @param uri   the contribution URI.
     * @param force true if the undeployment operation should ignore errors from runtimes and remove logical components on the controller. If true,
     *              undeployment will also succeed if no participants are available.
     * @throws DeploymentManagementException if an exception undeploying the contribution is encountered
     */
    void undeploy(URI uri, boolean force) throws DeploymentManagementException;

    /**
     * Returns a list of ComponentInfo instances representing the components deployed to the given composite path. The path "/" is interpreted as the
     * domain composite.
     *
     * @param path the path
     * @return the components
     * @throws InvalidPathException if the path is not found
     */
    List<ComponentInfo> getDeployedComponents(String path) throws InvalidPathException;

}

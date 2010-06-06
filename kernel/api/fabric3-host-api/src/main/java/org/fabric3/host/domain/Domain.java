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
package org.fabric3.host.domain;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.model.type.component.Composite;

/**
 * Represents a domain.
 *
 * @version $Rev$ $Date$
 */
public interface Domain {

    /**
     * Include a deployable composite in the domain.
     *
     * @param deployable the name of the deployable composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable) throws DeploymentException;

    /**
     * Include a deployable composite in the domain using the specified DeploymentPlan.
     *
     * @param deployable the name of the deployable composite to include
     * @param plan       the deployment plan name
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable, String plan) throws DeploymentException;

    /**
     * Include all deployables contained in the list of contributions in the domain. If deployment plans are present in the composites, they will be
     * used. This operation is intended for composites that are synthesized from multiple deployable composites that are associated with individual
     * deployment plans.
     *
     * @param uris the contributions to deploy
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(List<URI> uris) throws DeploymentException;

    /**
     * Include a composite in the domain.
     *
     * @param composite the composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(Composite composite) throws DeploymentException;

    /**
     * Remove a deployable Composite from the domain.
     *
     * @param deployable the name of the deployable composite to remove
     * @throws DeploymentException if an error is encountered during undeployment
     */
    void undeploy(QName deployable) throws DeploymentException;

    /**
     * Remove a deployable Composite from the domain.
     *
     * @param deployable the name of the deployable composite to remove
     * @param force      true if the undeployment operation should ignore errors from runtimes and remove logical components on the controller. If
     *                   true, undeployment will also succeed if no participants are available.
     * @throws DeploymentException if an error is encountered during undeployment
     */
    void undeploy(QName deployable, boolean force) throws DeploymentException;

    /**
     * Activates a set of definitions contained in the contribution.
     *
     * @param uri the contribution URI
     * @throws DeploymentException if an error is encountered durng activation
     */
    void activateDefinitions(URI uri) throws DeploymentException;

    /**
     * Deactivates a set of definitions contained in the contribution.
     *
     * @param uri the contribution URI
     * @throws DeploymentException if an error is encountered durng activation
     */
    void deactivateDefinitions(URI uri) throws DeploymentException;

    /**
     * Initiates a recovery operation using a Map of deployables to plans.
     *
     * @param deployables a Map containing deployable keys and plan name values
     * @throws DeploymentException if an error is encountered during recovery
     */
    void recover(Map<QName, String> deployables) throws DeploymentException;

}

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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.api.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.model.plan.DeploymentPlan;

/**
 * Provides utility functions for working with contributions.
 */
public interface ContributionHelper {

    /**
     * Returns the list of deployable composites contained in the list of contributions that are configured to run in the current runtime mode.
     *
     * @param contributions the contributions containing the deployables
     * @return the list of deployables
     */
    List<Composite> getDeployables(Set<Contribution> contributions);

    /**
     * Finds a deployable by name.
     *
     * @param deployable the deployable name
     * @return the deployable
     * @throws DeploymentException if the deployable cannot be resolved
     */
    Composite findComposite(QName deployable) throws DeploymentException;

    /**
     * Resolves the default plan for a deployable.
     *
     * @param deployable the deployable composite
     * @return the default plan or null if none is found
     */
    DeploymentPlan findDefaultPlan(QName deployable);

    /**
     * Resolves the default plan for a contribution.
     *
     * @param contribution the contribution
     * @return the default plan of null if none is found
     */
    DeploymentPlan findDefaultPlan(Contribution contribution);

    /**
     * Resolves a deployment plan by name.
     *
     * @param plan the deployment plan name
     * @return the resolved deployment plan or null if not found
     * @throws DeploymentException if the plan cannot be resolved
     */
    DeploymentPlan findPlan(String plan) throws DeploymentException;

    /**
     * Resolves the contributions from the list of URIs.
     *
     * @param uris the contribution  URIs
     * @return the set of contributions
     */
    Set<Contribution> findContributions(List<URI> uris);

    /**
     * Locks a set of contributions. The lock owners are the deployables in the contribution.
     *
     * @param contributions the contributions
     * @throws CompositeAlreadyDeployedException
     *          if a deployable is already deployed
     */
    void lock(Set<Contribution> contributions) throws CompositeAlreadyDeployedException;

    /**
     * Releases locks held on a set of contributions.
     *
     * @param contributions the contributions to release locks on
     */
    void releaseLocks(Set<Contribution> contributions);

}

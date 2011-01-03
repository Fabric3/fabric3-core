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
*/
package org.fabric3.admin.controller;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.management.domain.ContributionNotFoundException;
import org.fabric3.management.domain.DeploymentManagementException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.lcm.LogicalComponentManager;

/**
 * @version $Rev$ $Date$
 */
@Management(name = "RuntimeDomain", group = "kernel", description = "Manages the runtime domain")
public class RuntimeDomainMBeanImpl extends AbstractDomainMBean {
    private ContributionService contributionService;

    public RuntimeDomainMBeanImpl(@Reference(name = "domain") Domain domain,
                                  @Reference ContributionService contributionService,
                                  @Reference MetaDataStore store,
                                  @Reference LogicalComponentManager lcm,
                                  @Reference HostInfo info,
                                  @Monitor DomainMBeanMonitor monitor) {
        super(domain, store, lcm, info, monitor);
        this.contributionService = contributionService;
    }

    @ManagementOperation(description = "Deploys a profile to the runtime")
    public void deployProfile(URI profileUri) throws DeploymentManagementException {
        List<URI> uris = contributionService.getContributionsInProfile(profileUri);
        try {
            for (Iterator<URI> it = uris.iterator(); it.hasNext();) {
                URI uri = it.next();
                Contribution contribution = store.find(uri);
                if (contribution.isLocked()) {
                    // only include contributions in the profile that were not previously deployed 
                    it.remove();
                }
            }
            domain.include(uris);
        } catch (DeploymentException e) {
            throw new DeploymentManagementException("Error deploying profile " + profileUri + ":" + e.getMessage());
        }
    }

    @ManagementOperation(description = "Undeploys a profile")
    public void undeployProfile(URI uri) throws DeploymentManagementException {
        // the contributions must be undeployed by dependency
        List<URI> uris = contributionService.getSortedContributionsInProfile(uri);
        for (URI contributionUri : uris) {
            Contribution contribution = store.find(contributionUri);
            if (contribution == null) {
                throw new ContributionNotFoundException("Contribution not found: " + contributionUri);
            }
            try {
                domain.undeploy(contributionUri, false);
            } catch (DeploymentException e) {
                reportError(contributionUri, e);
            }
        }

    }


}
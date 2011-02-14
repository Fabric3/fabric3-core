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
package org.fabric3.management.rest.framework.domain.deployment;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.host.domain.ContributionNotInstalledException;
import org.fabric3.host.domain.DeployableNotFoundException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.management.domain.ContributionNotInstalledManagementException;
import org.fabric3.management.domain.InvalidDeploymentException;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.framework.domain.contribution.ContributionStatus;
import org.fabric3.management.rest.model.HttpHeaders;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Handles the /domain/contributions resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /deployments - Returns deployed contributions</ul>
 *  <li>PUT /deployments/contribution/{uri} - Deploys a contribution</ul>
 *  <li>DELETE /deployments/contribution/{uri} - Un-deploys a contribution</ul>
 * </ul>
 * </pre>
 * <p/>
 * Note this resource is only present on the controller.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
@EagerInit
@Management(path = "/domain/deployments")
public class DeploymentsResourceService {
    private Domain domain;
    private DomainResourceMonitor monitor;
    private MetaDataStore store;

    public DeploymentsResourceService(@Reference(name = "domain") Domain domain,
                                      @Reference MetaDataStore store,
                                      @Monitor DomainResourceMonitor monitor) {
        this.domain = domain;
        this.monitor = monitor;
        this.store = store;
    }

    @ManagementOperation(path = "/")
    public Resource getDeployments(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        List<URI> list = new ArrayList<URI>();
        for (Contribution contribution : contributions) {
            if (contribution.getLockOwners().isEmpty() && !contribution.getManifest().isExtension()) {
                // not deployed or not deployed to the application domain
                continue;
            }
            URI uri = contribution.getUri();
            list.add(uri);
        }
        resource.setProperty("contributions", list);
        return resource;
    }

    @ManagementOperation(type = OperationType.POST, path = "contribution")
    public Response deploy(HttpServletRequest request) throws ResourceException {
        String path = request.getPathInfo();
        int pos = path.lastIndexOf("/");
        String name = path.substring(pos + 1);
        try {
            URI uri = new URI(name);  // remove the leading "/"

            Contribution contribution = store.find(uri);
            if (contribution == null) {
                throw new ResourceException(HttpStatus.NOT_FOUND, "Contribution not found: " + uri);
            }
            try {
                domain.activateDefinitions(uri);
            } catch (DeploymentException e) {
                throw new AssertionError(e);
//                throw new ContributionNotInstalledManagementException(e.getMessage());
            }
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName deployableName = deployable.getName();
                try {
                  //  if (plan == null) {
                        domain.include(deployableName);
                  //  } else {
                  //      domain.include(deployableName, plan);
                  //  }
                } catch (ContributionNotInstalledException e) {
                    throw new AssertionError(e);
//                    throw new ContributionNotInstalledManagementException(e.getMessage());
                } catch (AssemblyException e) {
                    List<String> errors = new ArrayList<String>();
                    for (AssemblyFailure error : e.getErrors()) {
                        errors.add(error.getMessage() + " (" + error.getContributionUri() + ")");
                    }
                    throw new AssertionError(e);
//                    throw new InvalidDeploymentException("Error deploying " + uri, errors);
                } catch (CompositeAlreadyDeployedException e) {
                    throw new AssertionError(e);
//                    throw new ContributionNotInstalledManagementException(e.getMessage());
                } catch (DeployableNotFoundException e) {
                    throw new AssertionError(e);
//                    throw new ContributionNotInstalledManagementException(e.getMessage());
                } catch (DeploymentException e) {
                    throw new AssertionError(e);
//                   reportError(uri, e);
                }

            }

            Response response = new Response(HttpStatus.CREATED);
            response.addHeader(HttpHeaders.LOCATION, path);
            return response;
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI:", e);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid contribution URI: " + name);
        }
    }

    @ManagementOperation(type = OperationType.DELETE, path = "contribution")
    public void undeploy(String uri) throws ResourceException {
        URI contributionUri = URI.create(uri);
        try {
            domain.undeploy(contributionUri, false);
        } catch (DeploymentException e) {
            // TODO report better error
            monitor.error("Error removing contribution: " + uri, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing contribution: " + uri);
        }
    }

    private Link createContributionLink(URI contributionUri, HttpServletRequest request) {
        String uri = contributionUri.toString();
        URL url = ResourceHelper.createUrl(request.getRequestURL().toString() + "/contribution/" + uri);
        return new Link(uri, EDIT_LINK, url);
    }


}

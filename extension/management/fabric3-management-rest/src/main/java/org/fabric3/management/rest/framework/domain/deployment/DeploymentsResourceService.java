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
 */
package org.fabric3.management.rest.framework.domain.deployment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.domain.AssemblyException;
import org.fabric3.api.host.failure.AssemblyFailure;
import org.fabric3.api.host.domain.CompositeAlreadyDeployedException;
import org.fabric3.api.host.domain.ContributionNotFoundException;
import org.fabric3.api.host.domain.DeployableNotFoundException;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpHeaders;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;

/**
 * Handles the /domain/deployments resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /deployments - Returns deployed contributions</ul>
 *  <li>PUT /deployments/contribution/{uri} - Deploys a contribution</ul>
 *  <li>DELETE /deployments/contribution/{uri} - Un-deploys a contribution</ul>
 * </ul>
 * </pre>
 * <p/>
 * Note this resource is only present on the controller.
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
        List<URI> list = new ArrayList<>();
        for (Contribution contribution : contributions) {
            if (contribution.getLockOwners().isEmpty() || contribution.getManifest().isExtension()) {
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
                monitor.error("Error activating definitions: " + uri, e);
                return new Response(HttpStatus.BAD_REQUEST, "Error activating definitions " + uri + ": " + e.getMessage());
            }
            for (Deployable deployable : contribution.getManifest().getDeployables()) {
                QName deployableName = deployable.getName();
                try {
                    domain.include(deployableName);
                } catch (AssemblyException e) {
                    List<String> errors = new ArrayList<>();
                    for (AssemblyFailure error : e.getErrors()) {
                        errors.add(error.getMessage() + " (" + error.getContributionUri() + ")");
                    }
                    return new Response(HttpStatus.VALIDATION_ERROR, errors);
                } catch (CompositeAlreadyDeployedException e) {
                    return new Response(HttpStatus.CONFLICT, "Composite already deployed: " + deployableName);
                } catch (DeployableNotFoundException e) {
                    return new Response(HttpStatus.NOT_FOUND, "Composite not found: " + deployableName);
                } catch (DeploymentException e) {
                    monitor.error("Error deploying composite " + deployableName, e);
                    return new Response(HttpStatus.INTERNAL_SERVER_ERROR, "Error deploying composite " + deployableName + ": " + e.getMessage());
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
        } catch (ContributionNotFoundException e) {
            throw new ResourceException(HttpStatus.NOT_FOUND);
        } catch (DeploymentException e) {
            monitor.error("Error removing contribution: " + uri, e);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Error removing contribution " + uri + ": " + e.getMessage());
        }
    }


}

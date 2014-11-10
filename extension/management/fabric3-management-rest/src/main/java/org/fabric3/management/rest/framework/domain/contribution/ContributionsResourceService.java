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
package org.fabric3.management.rest.framework.domain.contribution;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.api.host.contribution.ContributionInUseException;
import org.fabric3.api.host.contribution.ContributionLockedException;
import org.fabric3.api.host.contribution.ContributionNotFoundException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.api.host.contribution.DuplicateContributionException;
import org.fabric3.api.host.contribution.InputStreamContributionSource;
import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.RemoveException;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.contribution.UninstallException;
import org.fabric3.api.host.contribution.UnsupportedContentTypeException;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpHeaders;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.api.host.contribution.ContributionAlreadyInstalledException;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.api.host.contribution.UnresolvedImportException;
import org.fabric3.spi.introspection.validation.InvalidContributionException;

import static org.fabric3.management.rest.model.Link.EDIT_LINK;

/**
 * Handles the /domain/contributions resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /contributions - Returns installed contributions</ul>
 *  <li>PUT /contributions/contribution/{uri} - Installs a contribution</ul>
 *  <li>GET /contributions/contribution/{uri} - Returns information on the installed contribution</ul>
 *  <li>DELETE /contributions/contribution/{uri} - Removes the installed contribution</ul>
 * </ul>
 * </pre>
 * <p/>
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/contributions")
public class ContributionsResourceService {
    private ContributionService contributionService;
    private MetaDataStore store;
    private ContributionsResourceMonitor monitor;

    public ContributionsResourceService(@Reference ContributionService contributionService,
                                        @Reference MetaDataStore store,
                                        @Monitor ContributionsResourceMonitor monitor) {
        this.contributionService = contributionService;
        this.store = store;
        this.monitor = monitor;
    }

    @ManagementOperation(path = "/")
    public Resource getContributions(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        List<ContributionStatus> list = new ArrayList<>();
        for (Contribution contribution : contributions) {
            URI uri = contribution.getUri();
            Link link = createContributionLink(uri, request);
            String state = contribution.getState().toString();
            ContributionStatus status = new ContributionStatus(uri, state, link);
            list.add(status);
        }
        resource.setProperty("contributions", list);
        return resource;
    }

    @ManagementOperation(path = "contribution")
    public Response createContribution(HttpServletRequest request) throws ResourceException {
        String path = request.getPathInfo();
        int pos = path.lastIndexOf("/");
        String name = path.substring(pos + 1);   // remove the leading "/"
        URI uri;
        try {
            uri = new URI(name);
            ContributionSource source = new InputStreamContributionSource(uri, request.getInputStream());
            contributionService.store(source);
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI: " + e.getReason());
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid contribution URI: " + name);
        } catch (IOException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating contribution: " + name);
        } catch (DuplicateContributionException e) {
            monitor.error("Duplicate contribution:" + name);
            throw new ResourceException(HttpStatus.CONFLICT, "Contribution already exists: " + name);
        } catch (StoreException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating contribution: " + name);
        }
        try {
            contributionService.install(uri);
            Response response = new Response(HttpStatus.CREATED);
            response.addHeader(HttpHeaders.LOCATION, path);
            return response;
        } catch (ContributionNotFoundException e) {
            String message = "Contribution not found: " + name;
            monitor.error(message);
            throw new ResourceException(HttpStatus.BAD_REQUEST, message);
        } catch (ContributionAlreadyInstalledException e) {
            String message = "Contribution already installed: " + name;
            monitor.error(message);
            throw new ResourceException(HttpStatus.BAD_REQUEST, message);
        } catch (UnresolvedImportException e) {
            String message = "The import " + e.getImport() + " could not be resolved: " + name;
            monitor.error(message);
            throw new ResourceException(HttpStatus.BAD_REQUEST, message);
        } catch (UnsupportedContentTypeException e) {
            String message = "Unknown contribution type: " + name;
            monitor.error(message);
            throw new ResourceException(HttpStatus.BAD_REQUEST, message);
        } catch (InvalidContributionException e) {
            ResourceException resourceException = new ResourceException(HttpStatus.VALIDATION_ERROR, "Invalid contribution: " + name);
            propagate(e, resourceException);
            throw resourceException;
        } catch (InstallException e) {
            String message = "Error creating contribution: " + name;
            monitor.error(message, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
    }

    @ManagementOperation(path = "contribution")
    public ContributionResource getContribution(String uri) throws ResourceException {
        URI contributionUri = URI.create(uri);
        Contribution contribution = store.find(contributionUri);
        if (contribution == null) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Contribution not found: " + uri);
        }
        String state = contribution.getState().toString();
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        List<QName> names = new ArrayList<>();
        for (Deployable deployable : deployables) {
            QName name = deployable.getName();
            names.add(name);
        }
        return new ContributionResource(contributionUri, state, names);
    }

    @ManagementOperation(path = "contribution")
    public void deleteContribution(String uri) throws ResourceException {
        URI contributionUri = URI.create(uri);
        try {
            contributionService.uninstall(contributionUri);
            contributionService.remove(contributionUri);
        } catch (ContributionInUseException e) {
            monitor.error("Contribution must be undeployed before it is uninstalled: " + uri);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Contribution must be undeployed before it is uninstalled: " + uri);
        } catch (ContributionLockedException e) {
            monitor.error("Unable to uninstall contribution in use: " + uri);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Unable to uninstall contribution in use: " + uri);
        } catch (UninstallException | RemoveException e) {
            monitor.error("Error removing contribution: " + uri, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing contribution: " + uri);
        } catch (ContributionNotFoundException e) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Contribution not found: " + uri);
        }
    }

    private Link createContributionLink(URI contributionUri, HttpServletRequest request) {
        String uri = contributionUri.toString();
        String requestUrl = ResourceHelper.getRequestUrl(request);
        URL url = ResourceHelper.createUrl(requestUrl + "/contribution/" + uri);
        return new Link(uri, EDIT_LINK, url);
    }

    private void propagate(InvalidContributionException e, ResourceException resourceException) {
        Map<String, List<String>> errors = new HashMap<>();
        for (ValidationFailure failure : e.getErrors()) {
            if (failure instanceof ArtifactValidationFailure) {
                ArtifactValidationFailure avf = (ArtifactValidationFailure) failure;
                List<String> artifactErrors = new ArrayList<>();
                errors.put(avf.getArtifactName(), artifactErrors);
                for (ValidationFailure entry : avf.getFailures()) {
                    artifactErrors.add(entry.getMessage());
                }
            } else {
                List<String> generalErrors = errors.get("General");
                if (generalErrors == null) {
                    generalErrors = new ArrayList<>();
                    errors.put("General", generalErrors);
                }
                generalErrors.add(failure.getMessage());
            }
        }
        resourceException.setEntity(errors);
    }

}

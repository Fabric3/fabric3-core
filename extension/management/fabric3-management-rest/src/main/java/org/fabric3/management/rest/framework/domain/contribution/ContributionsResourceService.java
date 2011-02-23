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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.DuplicateContributionException;
import org.fabric3.host.contribution.InputStreamContributionSource;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.UninstallException;
import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpHeaders;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
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
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
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
        List<ContributionStatus> list = new ArrayList<ContributionStatus>();
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
        try {
            URI uri = new URI(name);
            ContributionSource source = new InputStreamContributionSource(uri, request.getInputStream());
            contributionService.store(source);
            contributionService.install(uri);
            Response response = new Response(HttpStatus.CREATED);
            response.addHeader(HttpHeaders.LOCATION, path);
            return response;
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI:", e);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid contribution URI: " + name);
        } catch (DuplicateContributionException e) {
            monitor.error("Duplicate contribution:" + name, e);
            throw new ResourceException(HttpStatus.CONFLICT, "Contribution already exists: " + name);
        } catch (InvalidContributionException e) {
            ResourceException resourceException = new ResourceException(HttpStatus.BAD_REQUEST, "Invalid contribution: " + name);
            propagate(e, resourceException);
            throw resourceException;
        } catch (ContributionException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating contribution: " + name);
        } catch (IOException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating contribution: " + name);
        }
    }

    @ManagementOperation(path = "contribution")
    public Resource getContribution(String uri) throws ResourceException {
        URI contributionUri = URI.create(uri);
        Contribution contribution = store.find(contributionUri);
        if (contribution == null) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Contribution not found: " + uri);
        }
        String state = contribution.getState().toString();
        List<Deployable> deployables = contribution.getManifest().getDeployables();
        List<QName> names = new ArrayList<QName>();
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
        } catch (UninstallException e) {
            // TODO report better error
            monitor.error("Error removing contribution: " + uri, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing contribution: " + uri);
        } catch (RemoveException e) {
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
        Map<String, List<String>> errors = new HashMap<String, List<String>>();
        for (ValidationFailure failure : e.getErrors()) {
            if (failure instanceof ArtifactValidationFailure) {
                ArtifactValidationFailure avf = (ArtifactValidationFailure) failure;
                List<String> artifactErrors = new ArrayList<String>();
                errors.put(avf.getArtifactName(), artifactErrors);
                for (ValidationFailure entry : avf.getFailures()) {
                    artifactErrors.add(entry.getMessage());
                }
            } else {
                List<String> generalErrors = errors.get("General");
                if (generalErrors == null) {
                    generalErrors = new ArrayList<String>();
                    errors.put("General", generalErrors);
                }
                generalErrors.add(failure.getMessage());
            }
        }
        resourceException.setEntity(errors);
    }

}

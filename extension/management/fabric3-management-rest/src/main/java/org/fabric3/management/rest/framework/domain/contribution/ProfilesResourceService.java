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

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.contribution.ContributionException;
import org.fabric3.api.host.contribution.ContributionNotFoundException;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.contribution.DuplicateContributionException;
import org.fabric3.api.host.contribution.RemoveException;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.contribution.UninstallException;
import org.fabric3.management.rest.framework.ResourceHelper;
import org.fabric3.management.rest.model.HttpHeaders;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.Response;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Handles the /domain/contributions/profiles resource and its sub-resources:
 * <pre>
 * <ul>
 *  <li>GET /contributions/profiles - Returns installed profiles</ul>
 *  <li>PUT /contributions/profiles/{uri} - Installs a profile</ul>
 *  <li>DELETE /contributions/profiles/{uri} - Removes the installed profile</ul>
 * </ul>
 * </pre>
 * <p/>
 * Note this resource is only present on the controller.
 */
@EagerInit
@Management(path = "/domain/contributions/profiles")
public class ProfilesResourceService {
    private static final String EXTENSIONS = "extensions";
    private ContributionService contributionService;
    private MetaDataStore store;
    private ContributionsResourceMonitor monitor;

    public ProfilesResourceService(@Reference ContributionService contributionService,
                                   @Reference MetaDataStore store,
                                   @Monitor ContributionsResourceMonitor monitor) {
        this.contributionService = contributionService;
        this.store = store;
        this.monitor = monitor;
    }

    @ManagementOperation(path = "/")
    public Resource getProfiles(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        Set<Contribution> contributions = store.getContributions();
        Set<URI> profiles = new HashSet<>();
        for (Contribution contribution : contributions) {
            profiles.addAll(contribution.getProfiles());
        }
        resource.setProperty("profiles", profiles);
        return resource;
    }

    @ManagementOperation(path = "profile")
    public Response createProfile(HttpServletRequest request) throws ResourceException {
        String path = request.getPathInfo();
        int pos = path.lastIndexOf("/");
        String name = path.substring(pos + 1);   // remove the leading "/"
        try {
            URI uri = new URI(name);
            ServletInputStream stream = request.getInputStream();
            store(uri, stream);
            contributionService.installProfile(uri);
            Response response = new Response(HttpStatus.CREATED);
            response.addHeader(HttpHeaders.LOCATION, path);
            return response;
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI:", e);
            throw new ResourceException(HttpStatus.BAD_REQUEST, "Invalid profile URI: " + name);
        } catch (DuplicateContributionException e) {
            monitor.error("Duplicate profile:" + name, e);
            throw new ResourceException(HttpStatus.CONFLICT, "Profile already exists: " + name);
        } catch (ContributionException | IOException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating profile: " + name);
        }
    }              

    @ManagementOperation(path = "profile")
    public void deleteProfile(String uri) throws ResourceException {
        URI profileUri = URI.create(uri);
        try {
            contributionService.uninstallProfile(profileUri);
            contributionService.removeProfile(profileUri);
        } catch (UninstallException e) {
            monitor.error("Error removing profile: " + uri, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing profile: " + uri);
        } catch (RemoveException e) {
            monitor.error("Error removing contribution: " + uri, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing profile: " + uri);
        } catch (ContributionNotFoundException e) {
            throw new ResourceException(HttpStatus.NOT_FOUND, "Profile not found: " + uri);
        }
    }

    private synchronized List<URI> store(URI profileUri, InputStream stream) throws IOException, URISyntaxException, StoreException {
        JarInputStream jarStream = null;
        try {
            jarStream = new JarInputStream(stream);
            JarEntry entry;
            List<URI> contributionUris = new ArrayList<>();
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String[] tokens = entry.getName().split("/");
                if (tokens.length == 2 && EXTENSIONS.equals(tokens[0])) {
                    URI contributionUri = new URI(tokens[1]);
                    if (!contributionService.exists(contributionUri)) {
                        // the contribution does not exist, otherwise skip it
                        ContributionSource contributionSource = new WrappedStreamContributionSource(contributionUri, jarStream, true);
                        contributionService.store(contributionSource);
                    }
                    contributionUris.add(contributionUri);
                }
            }
            // add the profile
            contributionService.registerProfile(profileUri, contributionUris);
            return contributionUris;
        } finally {
            try {
                if (jarStream != null) {
                    jarStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

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
package org.fabric3.management.rest.framework.domain.contribution;

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
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

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
        Set<URI> profiles = new HashSet<URI>();
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
        } catch (ContributionException e) {
            monitor.error("Error creating contribution: " + name, e);
            throw new ResourceException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating profile: " + name);
        } catch (IOException e) {
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
            List<URI> contributionUris = new ArrayList<URI>();
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

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
package org.fabric3.admin.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.DuplicateProfileException;
import org.fabric3.host.contribution.StoreException;

/**
 * Responsible for soring contributions in a profile with the ContributionService.
 *
 * @version $Rev$ $Date$
 */
public class ProfileServlet extends HttpServlet {
    private static final long serialVersionUID = -8286023912719635905L;
    private static final String REPOSITORY = "repository";
    private ContributionService contributionService;
    private ContributionServiceMBeanMonitor monitor;

    public ProfileServlet(ContributionService contributionService, ContributionServiceMBeanMonitor monitor) {
        this.contributionService = contributionService;
        this.monitor = monitor;
    }


    /**
     * Stores a the contents of a profile via an HTTP POST operation.
     *
     * @param req  the servlet request
     * @param resp the servlet response
     * @throws ServletException if an unrecoverable error occurs processing the contribution.
     * @throws IOException      if an unrecoverable error occurs storing the contribution.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>No path info</description>");
            return;
        }
        int pos = path.lastIndexOf("/");
        if (pos < 0) {
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid path: " + path + " </description>");
            return;
        }
        String substr = path.substring(pos + 1);
        try {
            if (!substr.endsWith(".jar") && !substr.endsWith(".zip")) {
                resp.setStatus(422);
                resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Profile must be a zip or jar</description>");
                return;
            }
            URI uri = new URI(substr);  // remove the leading "/"
            if (contributionService.profileExists(uri)) {
                resp.setStatus(420);
                return;
            }
            store(uri, req.getInputStream());
            resp.setStatus(201);
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI:", e);
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid URI: " + substr + "</description>");
        } catch (DuplicateProfileException e) {
            resp.setStatus(420);
        } catch (IOException e) {
            monitor.error("Error storing contribution:", e);
            resp.setStatus(422);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing profile</description>");
        } catch (StoreException e) {
            monitor.error("Error storing contribution:", e);
            resp.setStatus(422);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing profile</description>");
        }
    }

    /**
     * Reads the profile stram and stores the contained contributions.
     *
     * @param profileUri the profile URI
     * @param stream     the profile input stream
     * @return the list of URIs of contributions stored from the profile
     * @throws IOException        if an error occurs reading the input stream
     * @throws URISyntaxException if a contained contribution name is invalid
     * @throws StoreException     if an error occurs during the store operation
     */
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
                if (tokens.length == 2 && REPOSITORY.equals(tokens[0])) {
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
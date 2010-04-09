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
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.DuplicateContributionException;

/**
 * @version $Rev$ $Date$
 */
public class ContributionServlet extends HttpServlet {
    private static final long serialVersionUID = -8286023912719635905L;

    private ContributionService contributionService;
    private ContributionServiceMBeanMonitor monitor;

    public ContributionServlet(ContributionService contributionService, ContributionServiceMBeanMonitor monitor) {
        this.contributionService = contributionService;
        this.monitor = monitor;
    }


    /**
     * Stores a contribution via an HTTP POST operation.
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
            URI uri = new URI(substr);  // remove the leading "/"
            ContributionSource source = new InputStreamContributionSource(uri, req.getInputStream());
            contributionService.store(source);
            resp.setStatus(200);
        } catch (URISyntaxException e) {
            monitor.error("Invalid contribution URI:", e);
            resp.setStatus(400);
            resp.getWriter().write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Invalid URI: " + substr + "</description>");
        } catch (DuplicateContributionException e) {
            resp.setStatus(420);
        } catch (ContributionException e) {
            monitor.error("Error storing contribution:", e);
            resp.setStatus(422);
            PrintWriter writer = resp.getWriter();
            writer.write("<?xml version=\"1.0\" encoding=\"ASCII\"?><description>Error storing contribution: " + e.getMessage() + "</description>");
        }
    }
}
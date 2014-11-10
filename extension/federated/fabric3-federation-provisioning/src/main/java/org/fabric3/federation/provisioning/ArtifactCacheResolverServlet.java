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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.federation.provisioning;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.fabric3.api.host.util.IOHelper;
import org.fabric3.spi.repository.ArtifactCache;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthorizationService;

/**
 * Used on a participant runtime to return the contents of a contribution associated with the encoded servlet path from the local artifact cache. The
 * servlet path corresponds to the contribution URI.
 */
public class ArtifactCacheResolverServlet extends AbstractResolverServlet {
    private static final long serialVersionUID = 7721634599080335126L;
    private transient ArtifactCache cache;
    private boolean secure;

    protected ArtifactCacheResolverServlet(ArtifactCache cache,
                                           AuthenticationService authenticationService,
                                           AuthorizationService authorizationService,
                                           String role,
                                           ProvisionMonitor monitor) {
        super(authenticationService, authorizationService, role, monitor);
        this.cache = cache;
        this.secure = true;
    }

    protected ArtifactCacheResolverServlet(ArtifactCache cache, ProvisionMonitor monitor) {
        super(null, null, null, monitor);
        this.cache = cache;
        this.secure = false;
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            monitor.errorMessage("Path info was null");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int pos = path.lastIndexOf("/");
        if (pos < 0) {
            monitor.errorMessage("Invalid path info");
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (secure && !checkAccess(req)) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String info = path.substring(pos + 1);

        try {
            URI uri = new URI(info);
            URL url = cache.get(uri);
            if (url == null) {
                monitor.errorMessage("Contribution not found: " + info + ". Request URL was: " + info);
                return;
            }
            IOHelper.copy(url.openStream(), resp.getOutputStream());
        } catch (URISyntaxException e) {
            monitor.error("Invalid URI: " + info, e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }


}
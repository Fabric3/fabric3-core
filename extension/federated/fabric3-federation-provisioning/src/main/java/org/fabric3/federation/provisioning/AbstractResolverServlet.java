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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthorizationException;
import org.fabric3.spi.security.AuthorizationService;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 */
public abstract class AbstractResolverServlet extends HttpServlet {
    private static final long serialVersionUID = 6804699201507293087L;
    protected transient AuthenticationService authenticationService;
    protected transient AuthorizationService authorizationService;
    protected String role;
    protected transient ProvisionMonitor monitor;


    protected AbstractResolverServlet(AuthenticationService authenticationService,
                                      AuthorizationService authorizationService,
                                      String role,
                                      ProvisionMonitor monitor) {
        this.authenticationService = authenticationService;
        this.authorizationService = authorizationService;
        this.role = role;
        this.monitor = monitor;
    }

    protected boolean checkAccess(HttpServletRequest req) {
        if (!req.getRequestURL().toString().toLowerCase().startsWith("https://")) {
            return false;
        }
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null) {
            return false;
        }
        try {
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            SecuritySubject subject = authenticationService.authenticate(token);
            authorizationService.checkRole(subject, role);
            return true;
        } catch (AuthenticationException e) {
            monitor.badAuthentication(e);
            return false;
        } catch (AuthorizationException e) {
            monitor.badAuthorization(e);
            return false;
        }
    }
}
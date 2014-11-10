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
package org.fabric3.fabric.security;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.util.Base64;

/**
 * Performs HTTP basic auth and populates the current work context with the authenticated subject.
 */
public class BasicAuthenticatorImpl implements BasicAuthenticator {
    private AuthenticationService authenticationService;

    @Reference(required = false)
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void authenticate(HttpServletRequest request, WorkContext context) throws AuthenticationException, NoCredentialsException {
        if (context.getSubject() != null) {
            // subject was previously authenticated
            return;
        }
        if (authenticationService == null) {
            throw new AuthenticationException("Authentication service not installed");
        }
        String header = request.getHeader("Authorization");
        if ((header == null) || !header.startsWith("Basic ")) {
            throw new NoCredentialsException();
        }
        String base64Token = header.substring(6);
        try {
            String decoded = new String(Base64.decode(base64Token), "UTF-8");
            String username = "";
            String password = "";
            int delimeter = decoded.indexOf(":");
            if (delimeter != -1) {
                username = decoded.substring(0, delimeter);
                password = decoded.substring(delimeter + 1);
            }
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            SecuritySubject subject = authenticationService.authenticate(token);
            context.setSubject(subject);
            // authorized
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

    }


}

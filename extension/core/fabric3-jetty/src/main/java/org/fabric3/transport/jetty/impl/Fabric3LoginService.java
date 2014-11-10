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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.transport.jetty.impl;

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 * Implementation that delegates authentication to the Fabric3 {@link AuthenticationService}.
 */
public class Fabric3LoginService implements LoginService {
    private AuthenticationService authenticationService;
    private IdentityService identityService = new DefaultIdentityService();

    public Fabric3LoginService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public String getName() {
        return "fabric3";
    }

    public UserIdentity login(String username, Object credentials) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, credentials.toString());
        try {
            SecuritySubject subject = authenticationService.authenticate(token);
            Subject jaasSubject = subject.getJaasSubject();
            Set<Role> roles = subject.getRoles();
            String[] roleNames = new String[roles.size()];
            int i = 0;
            for (Role role : roles) {
                roleNames[i] = role.getName();
                i++;
            }
            UserPrincipal principal = new UserPrincipal(username);
            return new DefaultUserIdentity(jaasSubject, principal, roleNames);
        } catch (AuthenticationException e) {
            // invalid token
            return null;
        }
    }

    public boolean validate(UserIdentity user) {
        return true;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(IdentityService service) {
        this.identityService = service;
    }

    public void logout(UserIdentity userIdentity) {

    }

    private class UserPrincipal implements Principal {
        private String name;

        private UserPrincipal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

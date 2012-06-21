/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
 *
 * @version $Rev$ $Date$
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

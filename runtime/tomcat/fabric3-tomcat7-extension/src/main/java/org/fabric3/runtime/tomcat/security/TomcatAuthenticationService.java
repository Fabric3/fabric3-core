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
package org.fabric3.runtime.tomcat.security;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.Realm;
import org.apache.catalina.realm.GenericPrincipal;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.runtime.tomcat.connector.ConnectorService;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 * Implementation which authenticates using a Tomcat security realm.
 */
@EagerInit
public class TomcatAuthenticationService implements AuthenticationService {
    private ConnectorService connectorService;
    private Realm realm;
    private AuthenticationService delegate;

    public TomcatAuthenticationService(@Reference ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    @Reference(required = false)
    public void setDelegate(AuthenticationService delegate) {
        this.delegate = delegate;
    }

    @Init()
    public void init() {
        realm = connectorService.getConnector().getService().getContainer().getRealm();
    }

    public SecuritySubject authenticate(AuthenticationToken<?, ?> token) throws AuthenticationException {
        if (delegate != null) {
            // if a security extension is installed, delegate to it
            return delegate.authenticate(token);
        }
        if (realm != null) {
            if (token instanceof UsernamePasswordToken) {
                UsernamePasswordToken usernamePassword = (UsernamePasswordToken) token;
                String username = usernamePassword.getPrincipal();
                String password = usernamePassword.getCredentials();
                Principal principal = realm.authenticate(username, password);
                if (principal instanceof GenericPrincipal) {
                    GenericPrincipal generic = (GenericPrincipal) principal;
                    Set<Role> roles = new HashSet<>();
                    for (String name : generic.getRoles()) {
                        roles.add(new Role(name));
                    }
                    return new BasicSecuritySubject(generic.getName(), generic.getPassword(), roles);
                } else {
                    return new BasicSecuritySubject(username, password, Collections.<Role>emptySet());
                }

            }

        }
        throw new AuthenticationException("Unable to authenticate because a Tomcat Realm or Fabric3 Security extension has not been configured");
    }
}

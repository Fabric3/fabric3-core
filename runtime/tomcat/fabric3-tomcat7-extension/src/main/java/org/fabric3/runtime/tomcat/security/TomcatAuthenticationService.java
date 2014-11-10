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

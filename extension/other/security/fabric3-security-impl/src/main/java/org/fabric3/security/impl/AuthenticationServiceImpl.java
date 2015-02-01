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
package org.fabric3.security.impl;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Basic authentication and service that relies on a SecurityStore for subject information. This implementation may also be used to authenticate JMX
 * credentials.
 */
@Service({AuthenticationService.class, JMXAuthenticator.class})
@EagerInit
public class AuthenticationServiceImpl implements AuthenticationService, JMXAuthenticator {
    private SecurityStore store;

    public AuthenticationServiceImpl(@Reference SecurityStore store) {
        this.store = store;
    }

    public SecuritySubject authenticate(AuthenticationToken<?, ?> token) throws AuthenticationException {
        if (token == null) {
            throw new IllegalArgumentException("Null token");
        }
        if (!(token instanceof UsernamePasswordToken)) {
            throw new UnsupportedOperationException("Token type not supported: " + token.getClass().getName());
        }
        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        String principal = userToken.getPrincipal();
        if (principal == null) {
            throw new AuthenticationException("Principal was null");
        }
        BasicSecuritySubject subject = store.find(principal);
        if (subject == null) {
            throw new InvalidAuthenticationException("Invalid authentication information");
        }
        if (!userToken.getCredentials().equals(subject.getPassword())) {
            throw new InvalidAuthenticationException("Invalid authentication information");
        }
        return subject;
    }

    public Subject authenticate(Object credentials) {
        if (!(credentials instanceof String[])) {
            if (credentials == null) {
                throw new SecurityException("Credentials were null");
            }
            throw new SecurityException("Credentials must be a String[]");
        }

        String[] params = (String[]) credentials;
        if (params.length != 2) {
            throw new SecurityException("Credentials must consist of a username and password");
        }
        UsernamePasswordToken token = new UsernamePasswordToken(params[0], params[1]);
        try {
            SecuritySubject subject = authenticate(token);
            return subject.getJaasSubject();
        } catch (AuthenticationException e) {
            throw new SecurityException(e);
        }

    }

}

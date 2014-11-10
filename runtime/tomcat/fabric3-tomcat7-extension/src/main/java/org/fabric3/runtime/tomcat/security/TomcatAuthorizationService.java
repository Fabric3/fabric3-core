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

import java.util.Collection;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.security.AuthorizationException;
import org.fabric3.spi.security.AuthorizationService;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.NotAuthorizedException;

/**
 * Implementation which uses Tomcat security to determine access rights.
 */
@EagerInit
public class TomcatAuthorizationService implements AuthorizationService {
    private AuthorizationService delegate;

    @Reference(required = false)
    public void setDelegate(AuthorizationService delegate) {
        this.delegate = delegate;
    }

    public void checkRole(SecuritySubject subject, String role) throws AuthorizationException {
        if (delegate != null) {
            delegate.checkRole(subject, role);
        }
        BasicSecuritySubject basicSubject = subject.getDelegate(BasicSecuritySubject.class);
        if (!basicSubject.hasRole(role)) {
            throw new NotAuthorizedException("Subject not authorized for role");
        }
    }

    public void checkRoles(SecuritySubject subject, Collection<String> roles) throws AuthorizationException {
        if (delegate != null) {
            delegate.checkRoles(subject, roles);
        }
        BasicSecuritySubject basicSubject = subject.getDelegate(BasicSecuritySubject.class);
        for (String role : roles) {
            if (!basicSubject.hasRole(role)) {
                throw new NotAuthorizedException("Subject not authorized for role");
            }
        }
    }

    public void checkPermission(SecuritySubject subject, String role) throws AuthorizationException {
        if (delegate != null) {
            delegate.checkPermission(subject, role);
        }
        throw new UnsupportedOperationException();
    }

    public void checkPermissions(SecuritySubject subject, Collection<String> roles) throws AuthorizationException {
        if (delegate != null) {
            delegate.checkPermissions(subject, roles);
        }
        throw new UnsupportedOperationException();
    }
}

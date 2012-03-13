/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * @version $Rev: 9216 $ $Date: 2010-07-19 10:15:48 +0200 (Mon, 19 Jul 2010) $
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

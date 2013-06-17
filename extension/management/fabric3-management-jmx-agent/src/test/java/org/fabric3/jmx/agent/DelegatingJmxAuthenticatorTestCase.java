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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.jmx.agent;

import java.util.Collections;
import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.Role;

/**
 *
 */
public class DelegatingJmxAuthenticatorTestCase extends TestCase {

    public void testAuthenticate() throws Exception {
        DelegatingJmxAuthenticator authenticator = new DelegatingJmxAuthenticator();
        JMXAuthenticator mockAuthenticator = EasyMock.createMock(JMXAuthenticator.class);
        EasyMock.expect(mockAuthenticator.authenticate(EasyMock.anyObject())).andReturn(new Subject());
        EasyMock.replay(mockAuthenticator);
        authenticator.setAuthenticators(Collections.singletonList(mockAuthenticator));
        authenticator.setRoles("ADMIN");
        authenticator.setSecurity(JmxSecurity.AUTHENTICATION.toString());
        authenticator.authenticate("credentials");
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public void testAuthenticateInvalidUser() throws Exception {
        DelegatingJmxAuthenticator authenticator = new DelegatingJmxAuthenticator();
        JMXAuthenticator mockAuthenticator = EasyMock.createMock(JMXAuthenticator.class);
        EasyMock.expect(mockAuthenticator.authenticate(EasyMock.anyObject())).andThrow(new SecurityException());
        EasyMock.replay(mockAuthenticator);
        authenticator.setAuthenticators(Collections.singletonList(mockAuthenticator));
        authenticator.setSecurity(JmxSecurity.AUTHENTICATION.toString());
        try {
            authenticator.authenticate("credentials");
            fail();
        } catch (SecurityException e) {
            // expected
        }
    }

    public void testAuthorized() throws Exception {
        DelegatingJmxAuthenticator authenticator = new DelegatingJmxAuthenticator();
        JMXAuthenticator mockAuthenticator = EasyMock.createMock(JMXAuthenticator.class);
        Subject subject = new Subject();
        Role role = new Role("ADMIN");
        subject.getPrincipals().add(role);
        EasyMock.expect(mockAuthenticator.authenticate(EasyMock.anyObject())).andReturn(subject);
        EasyMock.replay(mockAuthenticator);
        authenticator.setAuthenticators(Collections.singletonList(mockAuthenticator));
        authenticator.setRoles("ADMIN");
        authenticator.setSecurity(JmxSecurity.AUTHORIZATION.toString());
        authenticator.authenticate("credentials");
    }

    public void testNotAuthorized() throws Exception {
        DelegatingJmxAuthenticator authenticator = new DelegatingJmxAuthenticator();
        JMXAuthenticator mockAuthenticator = EasyMock.createMock(JMXAuthenticator.class);
        Subject subject = new Subject();
        Role role = new Role("NOT_AUTHORIZED");
        subject.getPrincipals().add(role);
        EasyMock.expect(mockAuthenticator.authenticate(EasyMock.anyObject())).andReturn(subject);
        EasyMock.replay(mockAuthenticator);
        authenticator.setAuthenticators(Collections.singletonList(mockAuthenticator));
        authenticator.setRoles("ADMIN");
        authenticator.setSecurity(JmxSecurity.AUTHORIZATION.toString());
        try {
            authenticator.authenticate("credentials");
            fail();
        } catch (SecurityException e) {
            // expected
        }
    }

}
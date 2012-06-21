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
*/
package org.fabric3.fabric.security;

import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 * @version $Rev: 9419 $ $Date: 2010-09-01 23:56:59 +0200 (Wed, 01 Sep 2010) $
 */
public class BasicAuthenticatorTestCase extends TestCase {

    public void testAuthenticate() throws Exception {
        AuthenticationService service = EasyMock.createMock(AuthenticationService.class);
        final SecuritySubject subject = new BasicSecuritySubject("test", "test", Collections.<Role>emptySet());
        EasyMock.expect(service.authenticate(EasyMock.isA(AuthenticationToken.class))).andStubAnswer(new IAnswer<SecuritySubject>() {
            public SecuritySubject answer() throws Throwable {
                UsernamePasswordToken token = (UsernamePasswordToken) EasyMock.getCurrentArguments()[0];

                // assert the basic auth header was properly decoded
                assertEquals("Aladdin", token.getPrincipal());
                assertEquals("open sesame", token.getCredentials());
                return subject;
            }
        });

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getHeader("Authorization")).andReturn("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==");

        EasyMock.replay(service, request);
        BasicAuthenticatorImpl authenticator = new BasicAuthenticatorImpl();
        authenticator.setAuthenticationService(service);

        WorkContext context = new WorkContext();

        authenticator.authenticate(request, context);

        EasyMock.verify(service, request);

        // assert the work context was updated with the subject
        assertNotNull(context.getSubject());
    }


    public void testNotAuthenticated() throws Exception {
        AuthenticationService service = EasyMock.createMock(AuthenticationService.class);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getHeader("Authorization")).andReturn(null);

        EasyMock.replay(service, request);
        BasicAuthenticatorImpl authenticator = new BasicAuthenticatorImpl();
        authenticator.setAuthenticationService(service);

        WorkContext context = new WorkContext();

        try {
            authenticator.authenticate(request, context);
            fail();
        } catch (NoCredentialsException e) {
            // expected
        }

        EasyMock.verify(service, request);

        // assert the work context was not updated with the subject
        assertNull(context.getSubject());
    }

}

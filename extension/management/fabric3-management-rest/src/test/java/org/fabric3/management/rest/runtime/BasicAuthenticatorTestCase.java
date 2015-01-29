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
package org.fabric3.management.rest.runtime;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.AuthenticationToken;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 *
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

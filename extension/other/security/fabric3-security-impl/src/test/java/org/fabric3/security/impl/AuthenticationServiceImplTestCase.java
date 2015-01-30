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

import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.Role;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.UsernamePasswordToken;

/**
 *
 */
public class AuthenticationServiceImplTestCase extends TestCase {

    public void testAuthenticate() throws Exception {
        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", Collections.<Role>emptySet());
        SecurityStore store = EasyMock.createMock(SecurityStore.class);
        EasyMock.expect(store.find(EasyMock.eq("foo"))).andReturn(subject);
        EasyMock.replay(store);
        AuthenticationService service = new AuthenticationServiceImpl(store);
        UsernamePasswordToken token = new UsernamePasswordToken("foo", "bar");
        service.authenticate(token);
    }

    public void testAuthenticateNameFail() throws Exception {
        SecurityStore store = EasyMock.createMock(SecurityStore.class);
        EasyMock.expect(store.find(EasyMock.eq("foo"))).andReturn(null);
        EasyMock.replay(store);
        AuthenticationService service = new AuthenticationServiceImpl(store);
        UsernamePasswordToken token = new UsernamePasswordToken("foo", "bar");
        try {
            service.authenticate(token);
            fail();
        } catch (AuthenticationException e) {
            // expected
        }
    }

    public void testAuthenticatePasswordFail() throws Exception {
        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", Collections.<Role>emptySet());
        SecurityStore store = EasyMock.createMock(SecurityStore.class);
        EasyMock.expect(store.find(EasyMock.eq("foo"))).andReturn(subject);
        EasyMock.replay(store);
        AuthenticationService service = new AuthenticationServiceImpl(store);
        UsernamePasswordToken token = new UsernamePasswordToken("foo", "baz");
        try {
            service.authenticate(token);
            fail();
        } catch (AuthenticationException e) {
            // expected
        }
    }

}

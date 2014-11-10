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
package org.fabric3.security.authentication;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.Role;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 *
 */
@EagerInit
public class CachingAuthenticationServiceTestCase extends TestCase {

    @SuppressWarnings({"unchecked"})
    public void testJson() throws Exception {
        Transformer transformer = EasyMock.createMock(Transformer.class);
        UsernamePasswordToken token = new UsernamePasswordToken();
        EasyMock.expect(transformer.transform(EasyMock.isA(InputStream.class), EasyMock.isA(ClassLoader.class))).andReturn(token);

        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);


        EasyMock.expect(registry.getTransformer(EasyMock.isA(DataType.class),
                                                EasyMock.isA(DataType.class),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(transformer);

        AuthenticationService authService = EasyMock.createMock(AuthenticationService.class);
        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", Collections.<Role>emptySet());
        EasyMock.expect(authService.authenticate(token)).andReturn(subject);
        ServletHost host = EasyMock.createNiceMock(ServletHost.class);

        HttpSession session = EasyMock.createMock(HttpSession.class);
        session.setAttribute("fabric3.subject", subject);
        EasyMock.expectLastCall();

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(request.getScheme()).andReturn("https");
        EasyMock.expect(request.getContentType()).andReturn("application/json");
        EasyMock.expect(request.getInputStream()).andReturn(new ServletInputStream() {
            public int read() {
                return 0;
            }
        });
        EasyMock.expect(request.getSession()).andReturn(session);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);

        EasyMock.replay(authService, transformer, registry, host, session, request, response);

        CachingAuthenticationService service = new CachingAuthenticationService(authService, registry, host, null);
        service.doPost(request, response);
        EasyMock.verify(authService, transformer, registry, host, session, request, response);
    }

    @SuppressWarnings({"unchecked"})
    public void testXml() throws Exception {
        Transformer transformer = EasyMock.createMock(Transformer.class);
        UsernamePasswordToken token = new UsernamePasswordToken();
        EasyMock.expect(transformer.transform(EasyMock.isA(InputStream.class), EasyMock.isA(ClassLoader.class))).andReturn(token);

        TransformerRegistry registry = EasyMock.createMock(TransformerRegistry.class);


        EasyMock.expect(registry.getTransformer(EasyMock.isA(DataType.class),
                                                EasyMock.isA(DataType.class),
                                                EasyMock.isA(List.class),
                                                EasyMock.isA(List.class))).andReturn(transformer);

        AuthenticationService authService = EasyMock.createMock(AuthenticationService.class);
        BasicSecuritySubject subject = new BasicSecuritySubject("foo", "bar", Collections.<Role>emptySet());
        EasyMock.expect(authService.authenticate(token)).andReturn(subject);
        ServletHost host = EasyMock.createNiceMock(ServletHost.class);

        HttpSession session = EasyMock.createMock(HttpSession.class);
        session.setAttribute("fabric3.subject", subject);
        EasyMock.expectLastCall();

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(request.getScheme()).andReturn("https");
        EasyMock.expect(request.getContentType()).andReturn("application/xml");
        EasyMock.expect(request.getInputStream()).andReturn(new ServletInputStream() {
            public int read() {
                return 0;
            }
        });
        EasyMock.expect(request.getSession()).andReturn(session);
        HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);

        EasyMock.replay(authService, transformer, registry, host, session, request, response);

        CachingAuthenticationService service = new CachingAuthenticationService(authService, registry, host, null);
        service.doPost(request, response);
        EasyMock.verify(authService, transformer, registry, host, session, request, response);
    }

}

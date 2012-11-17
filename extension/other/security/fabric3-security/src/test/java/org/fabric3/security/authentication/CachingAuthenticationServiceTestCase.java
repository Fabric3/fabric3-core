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
import org.fabric3.model.type.contract.DataType;
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

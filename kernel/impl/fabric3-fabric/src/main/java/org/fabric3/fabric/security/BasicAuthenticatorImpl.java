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

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.fabric3.spi.util.Base64;

/**
 * Performs HTTP basic auth and populates the current work context with the authenticated subject.
 */
public class BasicAuthenticatorImpl implements BasicAuthenticator {
    private AuthenticationService authenticationService;

    @Reference(required = false)
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void authenticate(HttpServletRequest request, WorkContext context) throws AuthenticationException, NoCredentialsException {
        if (context.getSubject() != null) {
            // subject was previously authenticated
            return;
        }
        if (authenticationService == null) {
            throw new AuthenticationException("Authentication service not installed");
        }
        String header = request.getHeader("Authorization");
        if ((header == null) || !header.startsWith("Basic ")) {
            throw new NoCredentialsException();
        }
        String base64Token = header.substring(6);
        try {
            String decoded = new String(Base64.decode(base64Token), "UTF-8");
            String username = "";
            String password = "";
            int delimeter = decoded.indexOf(":");
            if (delimeter != -1) {
                username = decoded.substring(0, delimeter);
                password = decoded.substring(delimeter + 1);
            }
            UsernamePasswordToken token = new UsernamePasswordToken(username, password);
            SecuritySubject subject = authenticationService.authenticate(token);
            context.setSubject(subject);
            // authorized
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }

    }


}

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
package org.fabric3.binding.rs.runtime;

import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;


/**
 * Dispatches an invocation from Jersey to the Fabric3 invocation chain fronting a component instance.
 *
 * @version $Rev$ $Date$
 */
public class RsMethodInterceptor implements MethodInterceptor {
    private static final String FABRIC3_SUBJECT = "fabric3.subject";

    private Map<String, InvocationChain> invocationChains;
    private BasicAuthenticator authenticator;

    public RsMethodInterceptor(Map<String, InvocationChain> invocationChains) {
        this.invocationChains = invocationChains;
    }

    public RsMethodInterceptor(Map<String, InvocationChain> invocationChains, BasicAuthenticator authenticator) {
        this.invocationChains = invocationChains;
        this.authenticator = authenticator;
    }

    public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        WorkContext context = WorkContextTunnel.getThreadWorkContext();
        authenticate(context);
        Message message = new MessageImpl(args, false, context);
        InvocationChain invocationChain = invocationChains.get(method.getName());
        if (invocationChain != null) {
            Interceptor headInterceptor = invocationChain.getHeadInterceptor();
            Message ret = headInterceptor.invoke(message);
            if (ret.isFault()) {
                throw (Throwable) ret.getBody();
            } else {
                return ret.getBody();
            }
        } else {
            return null;
        }
    }

    private void authenticate(WorkContext context) {
        if (authenticator == null) {
            // authentication is not required
            return;
        }
        HttpServletRequest request = (HttpServletRequest) context.getHeaders().get("fabric3.httpRequest");
        if (!"https".equals(request.getScheme())) {
            // authentication must be done over HTTPS
            //throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        // check if the subject was cached in the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            SecuritySubject subject = (SecuritySubject) session.getAttribute(FABRIC3_SUBJECT);
            if (subject != null) {
                context.setSubject(subject);
                return;
            }
        }
        try {
            authenticator.authenticate(request, context);
        } catch (NoCredentialsException e) {
            Response rsResponse = Response.status(UNAUTHORIZED).header("WWW-Authenticate", "Basic realm=\"fabric3\"").build();
            throw new WebApplicationException(rsResponse);
        } catch (AuthenticationException e) {
            throw new WebApplicationException(FORBIDDEN);
        }
    }

}


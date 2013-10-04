package org.fabric3.binding.rs.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.NotAuthorizedException;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.oasisopen.sca.ServiceRuntimeException;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Dispatches an invocation from Jersey to a component's interceptor chains.
 * <p/>
 * This instance assumes the resource instance is a Map of operation names to interceptor chains for the component.
 */
public class F3ResourceHandler {
    private static final String FABRIC3_SUBJECT = "fabric3.subject";

    private Class<?> interfaze;
    private Map<String, InvocationChain> chains;
    private boolean authenticate;
    private BasicAuthenticator authenticator;

    /**
     * Constructor.
     *
     * @param interfaze     the resource interface
     * @param chains        the invocation chains
     * @param authenticate  true if clients must authenticate
     * @param authenticator the authenticator to perform authentication with.
     */
    public F3ResourceHandler(Class<?> interfaze, Map<String, InvocationChain> chains, boolean authenticate, BasicAuthenticator authenticator) {
        this.interfaze = interfaze;
        this.chains = chains;
        this.authenticate = authenticate;
        this.authenticator = authenticator;
    }

    public Class<?> getInterface() {
        return interfaze;
    }

    public Object invoke(Method method, Object[] args) throws Throwable {
        InvocationChain invocationChain = chains.get(method.getName());
        if (invocationChain == null) {
            throw new ServiceRuntimeException("Unknown resource method: " + method.toString());
        }
        WorkContext context = WorkContextCache.getThreadWorkContext();   // work context set previously in RsContainer
        Message message = MessageCache.getAndResetMessage();

        try {
            if (authenticate) {
                authenticate(context);
            }

            message.setWorkContext(context);
            message.setBody(args);

            if (invocationChain != null) {
                Interceptor headInterceptor = invocationChain.getHeadInterceptor();
                Message ret = headInterceptor.invoke(message);
                if (ret.isFault()) {
                    return handleFault(ret);
                } else {
                    return ret.getBody();
                }
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            throw new InvocationTargetException(e);
        } finally {
            message.reset();
        }
    }

    private Object handleFault(Message ret) throws InvocationTargetException {
        if (ret.getBody() instanceof ServiceRuntimeException) {
            ServiceRuntimeException e = (ServiceRuntimeException) ret.getBody();
            if (e.getCause() instanceof NotAuthorizedException) {
                // authorization exceptions need to be mapped to a client 403 response
                throw new InvocationTargetException(new WebApplicationException(Response.Status.FORBIDDEN));
            }
            throw new InvocationTargetException(e);
        }
        throw new InvocationTargetException((Throwable) ret.getBody());
    }

    private void authenticate(WorkContext context) {
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

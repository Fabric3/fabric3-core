package org.fabric3.binding.rs.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.sun.jersey.spi.container.JavaMethodInvoker;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.BasicAuthenticator;
import org.fabric3.spi.security.NoCredentialsException;
import org.fabric3.spi.security.NotAuthorizedException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * Dispatches an invocation from Jersey to a component's interceptor chains. Replaces the default Jersey invoker which reflectively calls a method on
 * a Java instance. Optionally performs authentication.
 * <p/>
 * This instance assumes the resource instance is a Map of operation names to interceptor chains for the component.
 */
public class F3MethodInvoker implements JavaMethodInvoker {
    private static final String FABRIC3_SUBJECT = "fabric3.subject";

    private BasicAuthenticator authenticator;

    /**
     * Constructor.
     *
     * @param authenticator the authenticator to perform authentication with.
     */
    public F3MethodInvoker(BasicAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @SuppressWarnings({"unchecked"})
    public Object invoke(Method method, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (!(instance instanceof ResourceInstance)) {
            throw new IllegalArgumentException("Resource instance must be a " + ResourceInstance.class);
        }
        ResourceInstance resourceInstance = (ResourceInstance) instance;
        Map<String, InvocationChain> chains = resourceInstance.getChains();
        InvocationChain invocationChain = chains.get(method.getName());
        WorkContext context = WorkContextTunnel.getThreadWorkContext();

        try {

            if (resourceInstance.authenticate()) {
                authenticate(context);
            }

            Message message = new MessageImpl(args, false, context);
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
        }
    }

    private Object handleFault(Message ret) throws InvocationTargetException {
        if (ret.getBody() instanceof ServiceRuntimeException){
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

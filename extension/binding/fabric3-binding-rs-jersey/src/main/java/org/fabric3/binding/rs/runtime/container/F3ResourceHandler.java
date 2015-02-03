package org.fabric3.binding.rs.runtime.container;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.security.NotAuthorizedException;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Dispatches an invocation from Jersey to a component's interceptor chains.  This instance assumes the resource instance is a Map of operation names to
 * interceptor chains for the component.
 */
public class F3ResourceHandler {

    private Class<?> interfaze;
    private Map<String, InvocationChain> chains;

    /**
     * Constructor.
     *
     * @param interfaze the resource interface
     * @param chains    the invocation chains
     */
    public F3ResourceHandler(Class<?> interfaze, Map<String, InvocationChain> chains) {
        this.interfaze = interfaze;
        this.chains = chains;
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

}

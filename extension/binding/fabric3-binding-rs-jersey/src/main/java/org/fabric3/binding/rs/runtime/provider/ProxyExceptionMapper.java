package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 *
 */
@Provider
@SuppressWarnings("unchecked")
public class ProxyExceptionMapper<E extends Exception> extends AbstractProxyProvider<ExceptionMapper<E>> implements ExceptionMapper<E> {

    public Response toResponse(E exception) {
        return getDelegate().toResponse(exception);
    }
}

package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Dispatches to a component-based <code>MessageBodyWriter</code>.
 *
 * This implementation performs a lazy lookup of the component instance since readers are provisioned with composite resources, which occurs before components
 * are provisioned.
 */
@Provider
@SuppressWarnings("unchecked")
public class ProxyMessageBodyWriter extends AbstractProxyProvider<MessageBodyWriter> implements MessageBodyWriter {

    public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return getDelegate().isWriteable(type, genericType, annotations, mediaType);

    }

    public long getSize(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return getDelegate().getSize(o, type, genericType, annotations, mediaType);

    }

    public void writeTo(Object o,
                        Class type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        getDelegate().writeTo(o, type, genericType, annotations, mediaType, httpHeaders, entityStream);

    }
}

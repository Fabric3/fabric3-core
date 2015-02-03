package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Dispatches to a component-based <code>MessageBodyReader</code>.
 *
 * This implementation performs a lazy lookup of the component instance since readers are provisioned with composite resources, which occurs before components
 * are provisioned.
 */
@Provider
@SuppressWarnings("unchecked")
public class ProxyMessageBodyReader extends AbstractProxyProvider<MessageBodyReader> implements MessageBodyReader {
    public boolean isReadable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return getDelegate().isReadable(type, genericType, annotations, mediaType);
    }

    public Object readFrom(Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return getDelegate().readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);

    }
}

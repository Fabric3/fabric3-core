package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dispatches to a component-based Context resolver for Jackson <code>ObjectMapper</code> instances.
 * <p/>
 * This implementation performs a lazy lookup of the component instance since resolvers are provisioned with composite resources, which occurs before components
 * are provisioned.
 */
@Provider
public class ProxyObjectMapperContextResolver extends AbstractProxyProvider<ContextResolver<ObjectMapper>> implements ContextResolver<ObjectMapper> {

    public ObjectMapper getContext(Class<?> type) {
        return getDelegate().getContext(type);
    }
}

package org.fabric3.spi.introspection.service;

/**
 * Extension point for remote service introspection.
 */
public interface ServiceIntrospectorExtension {

    /**
     * Introspects a class to determine if it exposes remote endpoints.
     *
     * @param clazz the class
     * @return true if it exposes remote endpoints
     */
    boolean exportsEndpoints(Class<?> clazz);

}

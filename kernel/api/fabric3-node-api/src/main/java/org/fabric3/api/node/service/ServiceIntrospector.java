package org.fabric3.api.node.service;

/**
 * Introspects a class to determine if it exposes remote endpoints.
 *
 * This class is intended for code that needs to integrate Fabric3 node containers with host environments.
 */
public interface ServiceIntrospector {

    /**
     * Introspects a class to determine if it exposes remote endpoints.
     *
     * @param clazz the class
     * @return true if it exposes remote endpoints
     */
    boolean exportsEndpoints(Class<?> clazz);

}

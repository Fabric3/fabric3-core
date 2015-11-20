package org.fabric3.binding.rs.runtime.container;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * Associates the current Jersey container request with the current thread of execution.
 */
public class ContainerRequestCache {
    private static final ThreadLocal<ContainerRequest> CONTEXT = new ThreadLocal<>();

    private ContainerRequestCache() {
    }

    public static ContainerRequest get() {
        ContainerRequest request = CONTEXT.get();
        if (request == null) {
            throw new IllegalStateException("Container request context is not set on thread context");
        }
        return request;
    }

    public static void set(ContainerRequest request) {
        CONTEXT.set(request);
    }

    public static void clear() {
        CONTEXT.remove();
    }

}

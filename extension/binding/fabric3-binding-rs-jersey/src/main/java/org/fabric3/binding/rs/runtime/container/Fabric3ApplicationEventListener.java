package org.fabric3.binding.rs.runtime.container;

import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

/**
 * Sets the container request on the current thread so it can be injected on components.
 */
public class Fabric3ApplicationEventListener implements ApplicationEventListener, RequestEventListener {
    public void onEvent(ApplicationEvent event) {
    }

    public RequestEventListener onRequest(RequestEvent requestEvent) {
        return this;
    }

    public void onEvent(RequestEvent event) {
        if (RequestEvent.Type.MATCHING_START == event.getType()) {
            ContainerRequestCache.set(event.getContainerRequest());
        } else if (RequestEvent.Type.FINISHED == event.getType()) {
            ContainerRequestCache.clear();
        }

    }

}

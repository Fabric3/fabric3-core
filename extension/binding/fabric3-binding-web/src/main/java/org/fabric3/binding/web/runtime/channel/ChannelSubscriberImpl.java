/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.binding.web.runtime.channel;

import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.fabric3.spi.container.channel.EventStream;

/**
 * Implements GET semantics for the RESTful publish/subscribe where a GET will either result in the creation of a websocket connection for clients that support
 * it, or a suspended comet connection. Subsequent events published to the channel will be pushed to all subscribed clients.
 */
public class ChannelSubscriberImpl implements ChannelSubscriber {
    private long timeout;
    private EventStream stream;

    /**
     * Constructor.
     *
     * @param stream  the event stream for the channel that is being subscribed to
     * @param timeout the client connection timeout
     */
    public ChannelSubscriberImpl(EventStream stream, long timeout) {
        this.stream = stream;
        this.timeout = timeout;
    }

    public void subscribe(HttpServletRequest request) {
        AtmosphereResource resource = (AtmosphereResource) request.getAttribute(ApplicationConfig.ATMOSPHERE_RESOURCE);
        if (resource == null) {
            throw new IllegalStateException("Web binding extension not properly configured");
        }
        if (resource.transport() == TRANSPORT.LONG_POLLING) {
            request.setAttribute(ApplicationConfig.RESUME_ON_BROADCAST, Boolean.TRUE);
            resource.suspend(timeout, false);
        } else {
            resource.suspend(timeout);
        }
    }

    public EventStream getEventStream() {
        return stream;
    }

    public int getSequence() {
        return 0;
    }

}

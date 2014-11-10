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

import javax.servlet.ServletInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.websocket.WebSocketEventListener;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.ContentTypes;
import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;
import org.fabric3.api.host.util.IOHelper;

/**
 * Manages incoming requests destined for a channel. This includes setting the broadcaster associated with the request, forwarding HTTP requests to additional
 * handlers or suspending the request it is websocket-based.
 */
public class ChannelWebSocketHandler extends AbstractReflectorAtmosphereHandler implements WebSocketEventListener {

    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String RESPONSE_CONTENT_TYPE = ContentTypes.TEXT_PLAIN + ";charset=" + ISO_8859_1;

    private BroadcasterManager broadcasterManager;
    private ChannelMonitor monitor;
    private PubSubManager pubSubManager;

    public ChannelWebSocketHandler(BroadcasterManager broadcasterManager, PubSubManager pubSubManager, ChannelMonitor monitor) {
        this.broadcasterManager = broadcasterManager;
        this.pubSubManager = pubSubManager;
        this.monitor = monitor;
    }

    public void onRequest(AtmosphereResource resource) throws IOException {
        AtmosphereRequest req = resource.getRequest();
        AtmosphereResponse res = resource.getResponse();
        String method = req.getMethod();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null) {
            return;
        }

        //get channel name
        String channel = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);

        // Suspend the response.
        if ("GET".equalsIgnoreCase(method)) {

            // Log all events on the console, including WebSocket events.
            resource.addEventListener(this);

            res.setContentType(RESPONSE_CONTENT_TYPE);

            ChannelSubscriber subscriber = pubSubManager.getSubscriber(channel);
            if (subscriber == null) {
                throw new IOException("Channel not found");
            }

            Broadcaster b = broadcasterManager.getChannelBroadcaster(channel, resource.getAtmosphereConfig());
            resource.setBroadcaster(b);
            try {
                subscriber.subscribe(req);
            } catch (PublishException e) {
                res.setStatus(500);
                monitor.error(e);
            }

        } else if ("POST".equalsIgnoreCase(method)) {
            String contentType = req.getContentType();
            if (contentType == null || contentType.equals("text/html")) {
                contentType = ContentTypes.DEFAULT;
            }
            String encoding = req.getCharacterEncoding();
            if (encoding == null) {
                encoding = ISO_8859_1;
            }
            try {
                ServletInputStream stream = req.getInputStream();
                String data = read(stream, encoding);
                EventWrapper wrapper = ChannelUtils.createWrapper(contentType, data);
                ChannelPublisher publisher = pubSubManager.getPublisher(channel);
                publisher.publish(wrapper);
            } catch (PublishDeniedException e) {
                res.setStatus(403);   // forbidden
            } catch (PublishException e) {
                res.setStatus(500);
                monitor.error(e);
            } catch (InvalidContentTypeException e) {
                res.setStatus(400);
                monitor.error(e);
            }
        }

    }

    private String read(InputStream stream, String encoding) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOHelper.copy(stream, outputStream);
        return outputStream.toString(encoding);
    }

    public void destroy() {
    }

    public void onSuspend(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
    }

    public void onResume(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
    }

    public void onDisconnect(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
    }

    public void onBroadcast(AtmosphereResourceEvent event) {
        monitor.eventing(event.toString());
    }

    public void onThrowable(AtmosphereResourceEvent event) {
        monitor.error(event.throwable());
    }

    public void onHandshake(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

    public void onClose(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

    public void onControl(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

    public void onDisconnect(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

    public void onConnect(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

    public void onMessage(WebSocketEvent event) {
        monitor.eventingWS(event.toString());
    }

}

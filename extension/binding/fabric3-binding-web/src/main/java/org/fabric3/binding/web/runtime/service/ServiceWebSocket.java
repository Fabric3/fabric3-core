/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.web.runtime.service;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.WebSocketProcessor;
import org.atmosphere.websocket.JettyWebSocketSupport;
import org.eclipse.jetty.websocket.WebSocket;

import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.ClosedAwareJettyWebSocketSupport;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;

import static org.fabric3.binding.web.runtime.service.ServiceConstants.FABRIC3_BROADCASTER;

/**
 * Handles setting up a websocket connection and receiving inbound messages destined for a service.
 *
 * @version $Rev$ $Date$
 */
public class ServiceWebSocket implements WebSocket {
    private InvocationChain chain;
    private String callbackUri;
    private BroadcasterManager broadcastManager;
    private HttpServletRequest request;
    private AtmosphereServlet servlet;
    private Broadcaster broadcaster;
    private String uuid;

    private WebSocketProcessor webSocketProcessor;
    private ServiceMonitor monitor;

    public ServiceWebSocket(InvocationChain chain,
                            String callbackUri,
                            BroadcasterManager broadcasterManager,
                            HttpServletRequest request,
                            AtmosphereServlet servlet,
                            ServiceMonitor monitor) {
        this.chain = chain;
        this.callbackUri = callbackUri;
        this.broadcastManager = broadcasterManager;
        this.request = request;
        this.servlet = servlet;
        this.monitor = monitor;
    }

    public void onConnect(Outbound outbound) {
        // Create a broadcaster unique to the client
        uuid = UUID.randomUUID().toString();
        broadcaster = broadcastManager.getServiceBroadcaster(uuid);
        JettyWebSocketSupport support = new ClosedAwareJettyWebSocketSupport(outbound);
        webSocketProcessor = new WebSocketProcessor(servlet, support);
        try {
            request.setAttribute(FABRIC3_BROADCASTER, broadcaster);
            webSocketProcessor.connect(request);
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    public void onMessage(byte bytes, String data) {
        // Construct the work context and send to the target service
        // Set the correlation id to the broadcaster UUID so callbacks can be routed to the correct broadcaster instance
        Object[] content = new Object[]{data};
        WorkContext context = new WorkContext();
        CallFrame frame = new CallFrame(callbackUri, uuid, null, null);
        context.addCallFrame(frame);
        // As an optimization, we add the callframe twice instead of two different frames for representing the service call and the binding invocation 
        context.addCallFrame(frame);
        MessageImpl message = new MessageImpl(content, false, context);

        // Invoke the service and return a response using the broadcaster for this web socket
        Message response = chain.getHeadInterceptor().invoke(message);
        broadcaster.broadcast(response.getBody());
    }

    public void onMessage(byte frame, byte[] data, int offset, int length) {
        onMessage(frame, new String(data, offset, length));
    }

    public void onDisconnect() {
        webSocketProcessor.close();
        broadcastManager.remove(uuid);
    }


}

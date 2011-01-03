/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.web.runtime.channel;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.WebSocketProcessor;
import org.atmosphere.websocket.JettyWebSocketSupport;
import org.eclipse.jetty.websocket.WebSocket;

import org.fabric3.binding.web.runtime.common.ClosedAwareJettyWebSocketSupport;
import org.fabric3.binding.web.runtime.common.ContentTypes;
import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;
import org.fabric3.spi.channel.EventWrapper;

/**
 * Handles setting up a websocket connection and receiving inbound messages destined for a channel.
 *
 * @version $Rev$ $Date$
 */
public class ChannelWebSocket implements WebSocket {
    private AtmosphereServlet servlet;
    private HttpServletRequest request;

    private String contentType;
    private WebSocketProcessor webSocketProcessor;
    private ChannelPublisher publisher;


    public ChannelWebSocket(AtmosphereServlet servlet, ChannelPublisher publisher, HttpServletRequest request) {
        this.servlet = servlet;
        this.publisher = publisher;
        this.request = request;
        contentType = request.getHeader("Content-Type");
        if (contentType == null) {
            contentType = ContentTypes.DEFAULT;
        }
    }

    public void onConnect(Outbound outbound) {
        JettyWebSocketSupport support = new ClosedAwareJettyWebSocketSupport(outbound);
        webSocketProcessor = new WebSocketProcessor(servlet, support);
        try {
            webSocketProcessor.connect(request);
        } catch (IOException e) {
            // TODO monitor failure
        }
    }

    public void onMessage(byte frame, String data) {
        try {
            EventWrapper wrapper = ChannelUtils.createWrapper(contentType, data);
            publisher.publish(wrapper);
        } catch (PublishException e) {
            e.printStackTrace();
            // TODO monitor
        } catch (InvalidContentTypeException e) {
            e.printStackTrace();
            // TODO monitor
        }
    }

    public void onMessage(byte frame, byte[] data, int offset, int length) {
        onMessage(frame, new String(data, offset, length));
    }

    public void onDisconnect() {
        webSocketProcessor.close();
    }


}

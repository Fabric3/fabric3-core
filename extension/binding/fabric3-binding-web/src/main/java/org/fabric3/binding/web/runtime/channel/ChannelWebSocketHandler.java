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
package org.fabric3.binding.web.runtime.channel;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.websocket.WebSocketHttpServletResponse;

import org.fabric3.binding.web.runtime.common.BroadcasterManager;

/**
 * Manages incoming requests destined for a channel. This includes setting the broadcaster associated with the request, forwarding HTTP requests to
 * additional handlers or suspending the request it is websocket-based.
 *
 * @version $Rev$ $Date$
 */
public class ChannelWebSocketHandler extends AbstractReflectorAtmosphereHandler {
    private AtmosphereHandler<HttpServletRequest, HttpServletResponse> handler;
    private BroadcasterManager broadcasterManager;

    public ChannelWebSocketHandler(AtmosphereHandler<HttpServletRequest, HttpServletResponse> handler, BroadcasterManager broadcasterManager) {
        this.handler = handler;
        this.broadcasterManager = broadcasterManager;
    }

    public void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> r) throws IOException {
        String path = r.getRequest().getPathInfo().substring(1); // strip leading "/"
        Broadcaster broadcaster = broadcasterManager.get(path);
        r.setBroadcaster(broadcaster);
        if (!r.getResponse().getClass().isAssignableFrom(WebSocketHttpServletResponse.class)) {
            // not a websocket request
            handler.onRequest(r);
        } else {
            r.suspend(-1, false);
        }
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) throws IOException {
        super.onStateChange(event);
    }
}

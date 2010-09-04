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
package org.fabric3.binding.web.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereResource;

import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.channel.EventStream;

import static org.atmosphere.cpr.AtmosphereServlet.ATMOSPHERE_RESOURCE;

/**
 * Receives incoming requests for a channel published as a web endpoint. This dispatcher implements GET semantics for the RESTful publish/subscribe
 * protocol, where clients are subscribed to receive events from the channel. A GET will either result in the creation of a websocket connection for
 * clients that support it, or a suspended comet connection. Subsequent events published to the channel will be pushed to all subscribed clients.
 *
 * @version $Rev$ $Date$
 */
public class ChannelSubscriber implements ChannelConnection {
    private List<EventStream> streams = new ArrayList<EventStream>();

    public ChannelSubscriber(EventStream stream) {
        streams.add(stream);
    }

    public void subscribe(HttpServletRequest request) throws ServletException, IOException {
        AtmosphereResource<?, ?> resource = (AtmosphereResource<?, ?>) request.getAttribute(ATMOSPHERE_RESOURCE);
        if (resource == null) {
            throw new IllegalStateException("Web binding extension not properly configured");
        }
        resource.suspend(-1);
    }

    public List<EventStream> getEventStreams() {
        return streams;
    }

    public void addEventStream(EventStream stream) {
        // no-op
    }

}

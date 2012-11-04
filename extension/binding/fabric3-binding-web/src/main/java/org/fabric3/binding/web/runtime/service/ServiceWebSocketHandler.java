/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResource.TRANSPORT;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;

import org.fabric3.binding.web.runtime.common.BroadcasterManager;

/**
 *
 */
public class ServiceWebSocketHandler extends AbstractReflectorAtmosphereHandler {

    private ServiceManager serviceManager;
    private BroadcasterManager broadcasterManager;
    private ServiceMonitor monitor;


    public ServiceWebSocketHandler(ServiceManager serviceManager, BroadcasterManager broadcasterManager, ServiceMonitor monitor) {
        this.serviceManager = serviceManager;
        this.broadcasterManager = broadcasterManager;
        this.monitor = monitor;
    }


    public void onRequest(AtmosphereResource resource) throws IOException {
        AtmosphereRequest req = resource.getRequest();
        AtmosphereResponse res = resource.getResponse();
        String method = req.getMethod();

        // Suspend the response.
        if ("GET".equalsIgnoreCase(method)) {
            UUID uuid = UUID.randomUUID();
            // Log all events on the console, including WebSocket events.
            WebSocketServiceListener listener = new WebSocketServiceListener(uuid, monitor, serviceManager);
            resource.addEventListener(listener);

            res.setContentType("text/html;charset=ISO-8859-1");

            Broadcaster b = broadcasterManager.getServiceBroadcaster(uuid.toString(), resource.getAtmosphereConfig());
            resource.setBroadcaster(b);

            if (resource.transport() == TRANSPORT.LONG_POLLING) {
                req.setAttribute(ApplicationConfig.RESUME_ON_BROADCAST, Boolean.TRUE);
                resource.suspend(-1, false);
            } else {
                resource.suspend(-1);
            }
        } else if ("POST".equalsIgnoreCase(method)) {
            res.setStatus(500);
            monitor.error(new UnsupportedOperationException("No inbound messages allowed."));
        }
    }

    public void destroy() {
    }


}

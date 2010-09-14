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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.CometSupportResolver;
import org.eclipse.jetty.websocket.WebSocket;

import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.binding.web.runtime.common.Fabric3CometSupportResolver;
import org.fabric3.spi.host.ServletHost;

/**
 * Receives incoming comet and websocket requests destined for a service. This class extends the AtmosphereServlet to provide custom
 * <code>CometSupportResolver</code> and <code>WebSocket</code> implementations.
 *
 * @version $Rev: 9436 $ $Date: 2010-09-10 17:13:50 +0200 (Fri, 10 Sep 2010) $
 */
public class ServiceGatewayServlet extends AtmosphereServlet {
    private static final long serialVersionUID = -5519309286029777471L;
    private ServiceManager serviceManager;
    private BroadcasterManager broadcasterManager;
    private ServletHost servletHost;
    private ServiceMonitor monitor;

    public ServiceGatewayServlet(ServiceManager serviceManager,
                                 BroadcasterManager broadcasterManager,
                                 ServletHost servletHost,
                                 ServiceMonitor monitor) {
        this.serviceManager = serviceManager;
        this.broadcasterManager = broadcasterManager;
        this.servletHost = servletHost;
        this.monitor = monitor;
    }

    @Override
    protected CometSupportResolver createCometSupportResolver() {
        return new Fabric3CometSupportResolver(servletHost, config);
    }

    @Override
    protected void loadConfiguration(ServletConfig config) {
        // no-op, required
    }

    @Override
    protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        String path = request.getPathInfo().substring(1);    // strip leading '/'
        ChainPair pair = serviceManager.get(path);
        return new ServiceWebSocket(pair.getChain(), pair.getCallbackUri(), broadcasterManager, request, this, monitor);
    }


}

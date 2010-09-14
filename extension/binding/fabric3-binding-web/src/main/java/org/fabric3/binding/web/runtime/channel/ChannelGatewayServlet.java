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

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.CometSupportResolver;
import org.eclipse.jetty.websocket.WebSocket;

import org.fabric3.binding.web.runtime.common.Fabric3CometSupportResolver;
import org.fabric3.spi.host.ServletHost;

/**
 * Receives incoming comet and websocket requests destined for a channel. This class extends the AtmosphereServlet to provide custom
 * <code>CometSupportResolver</code> and <code>WebSocket</code> implementations.
 *
 * @version $Rev$ $Date$
 */
public class ChannelGatewayServlet extends AtmosphereServlet {
    private static final long serialVersionUID = -5519309286029777471L;
    private ServletHost servletHost;
    private PubSubManager pubSubManager;

    public ChannelGatewayServlet(ServletHost servletHost, PubSubManager pubSubManager) {
        this.servletHost = servletHost;
        this.pubSubManager = pubSubManager;
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
    protected WebSocket doWebSocketConnect(HttpServletRequest request, final String protocol) {
        String path = request.getPathInfo().substring(1);    // strip leading '/'
        ChannelPublisher publisher = pubSubManager.getPublisher(path);
        if (publisher == null) {
            // TODO return 404
            throw new AssertionError("Path not found");
        }
        return new ChannelWebSocket(this, publisher, request);
    }

    @Override
    public void destroy() {
        // avoid class not found exception thrown by atmosphere
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader loader = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            super.destroy();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}

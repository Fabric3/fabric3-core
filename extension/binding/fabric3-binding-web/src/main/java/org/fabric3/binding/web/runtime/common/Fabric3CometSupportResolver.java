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
package org.fabric3.binding.web.runtime.common;

import org.atmosphere.container.Jetty8WebSocketSupport;
import org.atmosphere.container.TomcatCometSupport;
import org.atmosphere.container.WebLogicCometSupport;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.AtmosphereServlet.AtmosphereConfig;
import org.atmosphere.cpr.CometSupport;
import org.atmosphere.cpr.CometSupportResolver;

import org.fabric3.spi.host.ServletHost;

/**
 * Returns an implementation of <code>CometSupport</code> for the runtime HTTP server.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3CometSupportResolver implements CometSupportResolver {
    private ServletHost servletHost;
    private AtmosphereConfig config;

    public Fabric3CometSupportResolver(ServletHost servletHost, AtmosphereConfig config) {
        this.servletHost = servletHost;
        this.config = config;
    }

    public CometSupport resolve(boolean useNativeIfPossible, boolean defaultToBlocking, boolean useWebsocketIfPossible) {
        Class<? extends CometSupport> clazz = detectWebSocketSupport();
        return instantiate(clazz);
    }

    private Class<? extends CometSupport> detectWebSocketSupport() {
        String type = servletHost.getHostType();
        if ("Jetty".equals(type)) {
            return Jetty8WebSocketSupport.class;
        } else if ("Tomcat".equals(type)) {
            return TomcatCometSupport.class;
        } else if ("WebLogic".equals(type)) {
            return WebLogicCometSupport.class;
        }
        throw new UnsupportedOperationException("Servlet container type not supported: " + type);
    }

    private CometSupport instantiate(Class<? extends CometSupport> clazz) {
        try {
            Class[] types = {AtmosphereServlet.AtmosphereConfig.class};
            return clazz.getDeclaredConstructor(types).newInstance(new Object[]{config});
        } catch (final Exception e) {
            throw new IllegalArgumentException("Comet support class " + clazz.getName() + " has an incorrect signature.", e);
        }
    }
}

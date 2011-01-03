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

package org.fabric3.binding.ws.metro.runtime.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.transport.http.servlet.ServletModule;
import com.sun.xml.wss.SecurityEnvironment;

/**
 * Implementation of the Metro host container SPI. Metro uses this SPI to obtain resources from the host container, in this case the Fabric3 runtime.
 *
 * @version $Rev$ $Date$
 */
public class F3Container extends Container {
    private static final String METRO_CONFIG = "metro-default.xml";
    private ServletContext servletContext;
    private SecurityEnvironment securityEnvironment;

    // Collection of active web service endpoints. Note this is updated by Metro (ServletAdaptor) using Module.getBoundResources() and hence there is
    // only a method for removing resources, which is not done by Metro and must be performed by the Fabric3 runtime.
    private List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

    private final ServletModule module = new ServletModule() {

        public List<BoundEndpoint> getBoundEndpoints() {
            return endpoints;
        }

        public String getContextPath() {
            throw new UnsupportedOperationException();
        }
    };

    private final ResourceLoader loader = new ResourceLoader() {
        public URL getResource(String resource) throws MalformedURLException {
            if (METRO_CONFIG.equals(resource)) {
                // return the Fabric3 custom metro tube configuration
                return getClass().getClassLoader().getResource("META-INF/f3-metro.xml");
            }
            return servletContext.getResource("/META-INF/" + resource);
        }
    };

    /**
     * Constructor.
     *
     * @param servletContext      the host servlet context
     * @param securityEnvironment the host security environment
     */
    public F3Container(ServletContext servletContext, SecurityEnvironment securityEnvironment) {
        this.servletContext = servletContext;
        this.securityEnvironment = securityEnvironment;
    }

    public void removeEndpoint(BoundEndpoint endpoint) {
        endpoints.remove(endpoint);
    }

    public <T> T getSPI(Class<T> spiType) {
        if (ServletContext.class.equals(spiType)) {
            return spiType.cast(servletContext);
        } else if (spiType.isAssignableFrom(SecurityEnvironment.class)) {
            return spiType.cast(securityEnvironment);
        } else if (spiType.isAssignableFrom(ServletModule.class)) {
            return spiType.cast(module);
        } else if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        }
        return null;
    }


}

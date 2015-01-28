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

package org.fabric3.binding.ws.metro.runtime.core;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.transport.http.servlet.ServletModule;

/**
 * Implementation of the Metro host container SPI. Metro uses this SPI to obtain resources from the host container, in this case the Fabric3 runtime.
 */
public class F3Container extends Container {
    private static final String METRO_CONFIG = "metro-default.xml";
    private ServletContext servletContext;

    // Collection of active web service endpoints. Note this is updated by Metro (ServletAdaptor) using Module.getBoundResources() and hence there is
    // only a method for removing resources, which is not done by Metro and must be performed by the Fabric3 runtime.
    private List<BoundEndpoint> endpoints = new ArrayList<>();

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
     * @param servletContext the host servlet context
     */
    public F3Container(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void removeEndpoint(BoundEndpoint endpoint) {
        endpoints.remove(endpoint);
    }

    public <T> T getSPI(Class<T> spiType) {
        if (ServletContext.class.equals(spiType)) {
            return spiType.cast(servletContext);
        } else if (spiType.isAssignableFrom(ServletModule.class)) {
            return spiType.cast(module);
        } else if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        }
        return null;
    }

}

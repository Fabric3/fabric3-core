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

import java.net.MalformedURLException;
import java.net.URL;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.wss.SecurityEnvironment;

/**
 * Implements the Metro Container SPI to return a custom SecurityEnvironment and resolve dynamically generated WSDL containing policy configuration
 * for a client (reference).
 */
public class WsitClientConfigurationContainer extends Container {
    private static final String CLIENT_CONFIG = "wsit-client.xml";
    private static final String METRO_CONFIG = "metro-default.xml";
    private URL wsitConfiguration;
    private SecurityEnvironment securityEnvironment;


    public WsitClientConfigurationContainer(URL wsitConfiguration, SecurityEnvironment securityEnvironment) throws MalformedURLException {
        this.securityEnvironment = securityEnvironment;
        this.wsitConfiguration = wsitConfiguration;
    }

    public WsitClientConfigurationContainer(SecurityEnvironment securityEnvironment) throws MalformedURLException {
        this.securityEnvironment = securityEnvironment;
    }

    private final ResourceLoader loader = new ResourceLoader() {
        public URL getResource(String resource) {
            if (wsitConfiguration != null && CLIENT_CONFIG.equals(resource)) {
                // if the Metro client configuration file is requested, return the generated WSDL
                return wsitConfiguration;
            } else if (METRO_CONFIG.equals(resource)) {
                // return the Fabric3 custom metro tube configuration
                return getClass().getClassLoader().getResource("META-INF/f3-metro.xml");
            }
            return null;
        }
    };

    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        } else if (spiType.isAssignableFrom(SecurityEnvironment.class)) {
            return spiType.cast(securityEnvironment);
        }
        return null;
    }


}
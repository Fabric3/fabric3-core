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
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.MalformedURLException;
import java.net.URL;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.wss.SecurityEnvironment;

/**
 * Implements the Metro Container SPI to return a custom SecurityEnvironment and resolve dynamically generated WSDL containing policy configuration
 * for a client (reference).
 *
 * @version $Rev$ $Date$
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
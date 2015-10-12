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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.host;

import javax.servlet.Servlet;
import java.net.URL;

/**
 * Interface implemented by host environments that allow Servlets to be registered.
 *
 * This interface allows an SCA system service to register a servlet to handle inbound requests.
 */
public interface ServletHost {

    /**
     * Returns the servlet host type, typically the container name.
     *
     * @return the servlet host type
     */
    String getHostType();

    /**
     * Returns the runtime HTTP.
     *
     * @return the runtime HTTP
     */
    int getHttpPort();

    /**
     * Returns the runtime HTTPS port.
     *
     * @return the runtime HTTPS port
     */
    int getHttpsPort();

    /**
     * Returns the base HTTP url of the servlet container.
     *
     * @return the base HTTP url of the servlet container.
     */
    URL getBaseHttpUrl();

    /**
     * Returns the base HTTP url of the servlet container.
     *
     * @return the base HTTP url of the servlet container.
     */
    URL getBaseHttpsUrl();

    /**
     * True if HTTPS is enabled.
     *
     * @return true if HTTPS is enabled
     */
    boolean isHttpsEnabled();

    /**
     * Register a mapping for an instance of a Servlet. This requests that the servlet container direct all requests to the designated mapping to the supplied
     * Servlet instance.
     *
     * @param mapping the uri-mapping for the Servlet
     * @param servlet the Servlet that should be invoked
     */
    void registerMapping(String mapping, Servlet servlet);

    /**
     * Unregister a servlet mapping. This directs the servlet contain not to direct any more requests to a previously registered Servlet.
     *
     * @param mapping the uri-mapping for the Servlet
     * @return the servlet that was registered to the mapping, null if nothing was registered to the mapping
     */
    Servlet unregisterMapping(String mapping);

    /**
     * Check to see if a mapping exists.
     *
     * @param mapping the uri-mapping for the Servlet
     * @return true if mapping is registered, false otherwise
     */
    boolean isMappingRegistered(String mapping);

    /**
     * Returns the servlet context path this host is registered under in the runtime.
     *
     * @return the path
     */
    default String getContextPath() {
        return "";
    }

}

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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.host;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

/**
 * Interface implemented by host environments that allow Servlets to be registered.
 * <p/>
 * This interface allows an SCA system service to register a servlet to handle inbound requests.
 *
 * @version $Rev$ $Date$
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
     * Returns the default servlet context associated with the host.
     * <p/>
     * NOTE: Remove when the webapp runtime is decommissioned
     *
     * @return the default servlet context associated with the host
     */
    @Deprecated
    ServletContext getServletContext();

    /**
     * True if HTTPS is enabled.
     *
     * @return true if HTTPS is enabled
     */
    boolean isHttpsEnabled();

    /**
     * Register a mapping for an instance of a Servlet. This requests that the servlet container direct all requests to the designated mapping to the
     * supplied Servlet instance.
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

}

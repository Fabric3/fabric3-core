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
package org.fabric3.container.web.spi;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import org.osoa.sca.ComponentContext;

import org.fabric3.spi.objectfactory.Injector;

/**
 * Responsible for activating a web application in an embedded servlet container.
 *
 * @version $Rev$ $Date$
 */
public interface WebApplicationActivator {
    public static final String SERVLET_CONTEXT_SITE = "fabric3.servletContext";
    public static final String SESSION_CONTEXT_SITE = "fabric3.sessionContext";
    public static final String CONTEXT_ATTRIBUTE = "org.osoa.sca.ComponentContext";
    public static final String OASIS_CONTEXT_ATTRIBUTE = "org.oasisopen.sca.ComponentContext";

    /**
     * Returns the classloader to use for the web component corresponding the given id
     *
     * @param componentId the web component id
     * @return the classloader
     */
    ClassLoader getWebComponentClassLoader(URI componentId);

    /**
     * Perform the activation, which will result in making the web application available for incoming requests to the runtime.
     *
     * @param contextPath         the context path the web application will be available at. The context path is relative to the absolute address of
     *                            the embedded servlet container.
     * @param uri                 the URI of the contribution containing the web application assets
     * @param parentClassLoaderId the id for parent classloader to use for the web application
     * @param injectors           the map of artifact ids to injectors. An artifact id identifies an artifact type such as a servlet class name or
     *                            ServletContext.
     * @param context             the component context for the web component
     * @return the servlet context associated with the activated web application
     * @throws WebApplicationActivationException
     *          if an error occurs activating the web application
     */
    ServletContext activate(String contextPath, URI uri, URI parentClassLoaderId, Map<String, List<Injector<?>>> injectors, ComponentContext context)
            throws WebApplicationActivationException;

    /**
     * Removes an activated web application
     *
     * @param uri the URI the web application was activated with
     * @throws WebApplicationActivationException
     *          if an error occurs activating the web application
     */
    void deactivate(URI uri) throws WebApplicationActivationException;

}

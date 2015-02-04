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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.container.web.spi;

import javax.servlet.ServletContext;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.injection.Injector;

/**
 * Responsible for activating a web application in an embedded servlet container.
 */
public interface WebApplicationActivator {
    public static final String SERVLET_CONTEXT_SITE = "fabric3.servletContext";
    public static final String SESSION_CONTEXT_SITE = "fabric3.sessionContext";
    public static final String OASIS_CONTEXT_ATTRIBUTE = "org.oasisopen.sca.ComponentContext";

    /**
     * Perform the activation, which will result in making the web application available for incoming requests to the runtime.
     *
     * @param contextPath       the context path the web application will be available at. The context path is relative to the absolute address of the embedded
     *                          servlet container.
     * @param uri               the URI of the contribution containing the web application assets
     * @param parentClassLoader the parent classloader to use for the web application
     * @param injectors         the map of artifact ids to injectors. An artifact id identifies an artifact type such as a servlet class name or
     *                          ServletContext.
     * @return the servlet context associated with the activated web application
     * @throws Fabric3Exception if an error occurs activating the web application
     */
    ServletContext activate(String contextPath, URI uri, ClassLoader parentClassLoader, Map<String, List<Injector<?>>> injectors) throws Fabric3Exception;

    /**
     * Removes an activated web application
     *
     * @param uri the URI the web application was activated with
     * @throws Fabric3Exception if an error occurs activating the web application
     */
    void deactivate(URI uri) throws Fabric3Exception;

}

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
package org.fabric3.runtime.weblogic.boot;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import java.io.IOException;

import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.runtime.weblogic.api.ServletRequestDispatcher;
import static org.fabric3.runtime.weblogic.api.Constants.RUNTIME_ATTRIBUTE;
import static org.fabric3.runtime.weblogic.api.ServletRequestDispatcher.SERVLET_REQUEST_DISPATCHER;

/**
 * Forwards incoming HTTP requests to a servlet provided by a binding.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class WebLogicDispatchingServlet extends HttpServlet {
    private static final long serialVersionUID = -7044395140732475283L;
    private ServletRequestDispatcher requestDispatcher;

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        Fabric3Runtime runtime = (Fabric3Runtime) servletContext.getAttribute(RUNTIME_ATTRIBUTE);
        if (runtime == null) {
            throw new ServletException("Fabric3 runtime not configured");
        }
        requestDispatcher = runtime.getComponent(ServletRequestDispatcher.class, SERVLET_REQUEST_DISPATCHER);
        if (requestDispatcher == null) {
            throw new ServletException(ServletRequestDispatcher.class.getSimpleName() + " not configured: " + SERVLET_REQUEST_DISPATCHER);
        }
        requestDispatcher.init(config);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        requestDispatcher.service(req, res);
    }
}
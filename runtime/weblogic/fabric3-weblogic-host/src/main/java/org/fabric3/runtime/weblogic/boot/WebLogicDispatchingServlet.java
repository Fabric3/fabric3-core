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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.weblogic.boot;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.fabric3.host.runtime.Fabric3Runtime;
import static org.fabric3.runtime.weblogic.api.Constants.RUNTIME_ATTRIBUTE;
import org.fabric3.runtime.weblogic.api.ServletRequestDispatcher;
import static org.fabric3.runtime.weblogic.api.ServletRequestDispatcher.SERVLET_REQUEST_DISPATCHER;

/**
 * Forwards incoming HTTP requests to a servlet provided by a binding.
 *
 * @version $Rev$ $Date$
 */
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
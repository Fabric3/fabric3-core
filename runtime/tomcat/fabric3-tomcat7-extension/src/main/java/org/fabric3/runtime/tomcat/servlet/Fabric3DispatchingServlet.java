/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.runtime.tomcat.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;

/**
 * A servlet registered in the Tomcat host runtime that forwards requests to other servlets. For example, servlets that handle requests destined to
 * services sent using HTTP-based bindings.
 */
public class Fabric3DispatchingServlet extends HttpServlet implements CometProcessor {
    private static final long serialVersionUID = -8765328474350267313L;

    private Map<String, Servlet> servlets = new ConcurrentHashMap<>();
    private ServletConfig config;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.config = config;
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        Servlet servlet = servlets.get(path);
        if (servlet == null) {
            int i;
            servlet = servlets.get(path + "/*");
            if (servlet == null) {
                while ((i = path.lastIndexOf("/")) >= 0) {
                    servlet = servlets.get(path.substring(0, i) + "/*");
                    if (servlet != null) {
                        req = new MappedHttpServletRequest(req, path.substring(0, i));
                        break;
                    }
                    path = path.substring(0, i);
                }
            }
            if (servlet == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("No servlet registered for path: " + req.getPathInfo());
                return;
            }
        }

        servlet.service(req, resp);
    }

    public void registerMapping(String path, Servlet servlet) throws ServletException {
        if (servlets.containsKey(path)) {
            throw new IllegalStateException("Servlet already registered at path: " + path);
        }
        servlet.init(config);
        servlets.put(path, servlet);
    }

    public Servlet unregisterMapping(String path) throws ServletException {
        Servlet servlet = servlets.remove(path);
        if (servlet == null) {
            throw new ServletException("Servlet not registered: " + path);
        }
        servlet.destroy();
        return servlet;
    }

    public void event(CometEvent event) throws IOException, ServletException {
        HttpServletRequest req = event.getHttpServletRequest();
        HttpServletResponse resp = event.getHttpServletResponse();
        String path = req.getPathInfo();
        Servlet servlet = servlets.get(path);
        if (servlet == null) {
            int i;
            servlet = servlets.get(path + "/*");
            if (servlet == null) {
                while ((i = path.lastIndexOf("/")) >= 0) {
                    servlet = servlets.get(path.substring(0, i) + "/*");
                    if (servlet != null) {
                        break;
                    }
                    path = path.substring(0, i);
                }
            }
            if (servlet == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("No servlet registered for path: " + req.getPathInfo());
                return;
            }
        }
        if (servlet instanceof CometProcessor) {
            ((CometProcessor) servlet).event(event);
        } else {
            servlet.service(req, resp);
        }
    }

    private class MappedHttpServletRequest extends HttpServletRequestWrapper {

        private String servletPath;

        public MappedHttpServletRequest(HttpServletRequest request, String servletPath) {
            super(request);
            this.servletPath = servletPath;
        }

        @Override
        public String getServletPath() {
            return servletPath;
        }
    }
}

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
package org.fabric3.runtime.tomcat.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometProcessor;

/**
 * A servlet registered in the Tomcat host runtime that forwards requests to other servlets. For example, servlets that handle requests destined to
 * services sent using HTTP-based bindings.
 */
public class Fabric3DispatchingServlet extends HttpServlet implements CometProcessor {
    private static final long serialVersionUID = -8765328474350267313L;

    private transient Map<String, Servlet> servlets = new ConcurrentHashMap<>();
    private transient ServletConfig config;

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

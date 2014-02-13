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
package org.fabric3.node;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fabric3.spi.host.ServletHost;

/**
 * Servlet host implementation used when the node runtime is deployed in a web application.
 */
public class FabricServletHost extends HttpServlet implements ServletHost {
    private static final long serialVersionUID = -9146911758634764053L;

    private int http;
    private int https;
    private URL baseUrl;
    private URL baseHttpsUrl;

    private transient ServletConfig config;

    private transient Map<String, Servlet> servlets = new ConcurrentHashMap<>();
    private AtomicBoolean initialized = new AtomicBoolean();

    public FabricServletHost(int http, int https, URL baseUrl, URL baseHttpsUrl) {
        this.http = http;
        this.https = https;
        this.baseUrl = baseUrl;
        this.baseHttpsUrl = baseHttpsUrl;
    }

    public String getHostType() {
        return "fabric";
    }

    public int getHttpPort() {
        return http;
    }

    public int getHttpsPort() {
        return https;
    }

    public URL getBaseHttpUrl() {
        return baseUrl;
    }

    public URL getBaseHttpsUrl() {
        return baseHttpsUrl;
    }

    public boolean isHttpsEnabled() {
        return https != -1;
    }

    public void init(ServletConfig config) throws ServletException {
        for (Servlet servlet : servlets.values()) {
            servlet.init(config);
        }
        this.config = config;
        initialized.set(true);
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) {
            notFound(resp);
            return;
        }
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
                notFound(resp);
                return;
            }
        }
        servlet.service(req, resp);
    }

    public void registerMapping(String path, Servlet servlet) {
        if (servlets.containsKey(path)) {
            throw new IllegalStateException("Servlet already registered at path: " + path);
        }
        if (initialized.get()) {
            // initialization done previously, initialize this servlet now
            try {
                servlet.init(config);
            } catch (ServletException e) {
                log("Error initializing servlet for path: " + path, e);
            }
        }
        servlets.put(path, servlet);
    }

    public boolean isMappingRegistered(String mapping) {
        return servlets.containsKey(mapping);

    }

    public Servlet unregisterMapping(String path) {
        return servlets.remove(path);
    }

    private void notFound(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        PrintWriter writer = resp.getWriter();
        writer.print("Resource not found");
        writer.close();
    }

}

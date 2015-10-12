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
    private String contextPath;

    private transient ServletConfig config;

    private transient Map<String, Servlet> servlets = new ConcurrentHashMap<>();
    private AtomicBoolean initialized = new AtomicBoolean();

    public FabricServletHost(int http, int https, URL baseUrl, URL baseHttpsUrl, String contextPath) {
        this.http = http;
        this.https = https;
        this.baseUrl = baseUrl;
        this.baseHttpsUrl = baseHttpsUrl;
        this.contextPath = contextPath;
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

    public String getContextPath() {
        return contextPath;
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

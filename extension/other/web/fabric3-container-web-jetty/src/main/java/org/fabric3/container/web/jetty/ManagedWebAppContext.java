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
package org.fabric3.container.web.jetty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.webapp.WebAppContext;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 *
 */
@Management
public class ManagedWebAppContext extends WebAppContext {
    public ManagedWebAppContext(String webAppDir, String contextPath) {
        super(webAppDir, contextPath);
    }

    @ManagementOperation(description = "The web app name")
    public String getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    @ManagementOperation(description = "The web app context path")
    public String getContextPath() {
        return super.getContextPath();
    }

    @Override
    @ManagementOperation(description = "If web app is available")
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    @ManagementOperation(description = "The web app state")
    public String getState() {
        return super.getState();
    }

    @ManagementOperation(description = "Start the web app")
    public void startWebApp() throws Exception {
        super.start();
    }

    @ManagementOperation(description = "Stop the web app")
    public void stopWebApp() throws Exception {
        super.stop();
    }

    @Override
    public String[] getVirtualHosts() {
        return super.getVirtualHosts();
    }

    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        try {
            WebRequestTunnel.setRequest(request);
            super.doHandle(target, baseRequest, request, response);
        } finally {
            WebRequestTunnel.setRequest(null);
            workContext.reset();
        }
    }
}
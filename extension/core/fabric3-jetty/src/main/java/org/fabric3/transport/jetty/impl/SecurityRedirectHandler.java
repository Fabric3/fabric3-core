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
package org.fabric3.transport.jetty.impl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

/**
 * Redirects HTTP requests to HTTPS.
 */
public class SecurityRedirectHandler extends HandlerWrapper {
    private int httpsPort;

    public SecurityRedirectHandler(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!request.isSecure()) {
            baseRequest.getResponse().sendRedirect("https://" + baseRequest.getServerName() + ":" + httpsPort + baseRequest.getPathInfo());
            baseRequest.setHandled(true);
        } else {
            getHandler().handle(target, baseRequest, request, response);
        }

    }
}

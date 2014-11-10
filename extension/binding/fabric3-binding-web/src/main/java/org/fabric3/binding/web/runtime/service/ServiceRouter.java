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
package org.fabric3.binding.web.runtime.service;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.AtmosphereResource;

import static org.atmosphere.cpr.FrameworkConfig.ATMOSPHERE_RESOURCE;

/**
 *
 */
public class ServiceRouter extends HttpServlet {
    private static final long serialVersionUID = -5280160176214956503L;
    private long timeout;

    /**
     * Constructor.
     *
     * @param timeout the client connection timeout
     */
    public ServiceRouter(long timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AtmosphereResource resource = (AtmosphereResource) request.getAttribute(ATMOSPHERE_RESOURCE);
        if (resource == null) {
            throw new IllegalStateException("Web binding extension not properly configured");
        }
        resource.suspend(timeout);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(500);
    }

}

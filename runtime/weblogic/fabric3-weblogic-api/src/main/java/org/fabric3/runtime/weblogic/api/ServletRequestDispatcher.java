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
package org.fabric3.runtime.weblogic.api;

import java.io.IOException;
import java.net.URI;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.fabric3.api.host.Names;

/**
 * Interface to a system component that dispatches servlet requests to the Fabric3 runtime
 */
public interface ServletRequestDispatcher {

    /**
     * Well-known URI of the dispatcher component
     */
    URI SERVLET_REQUEST_DISPATCHER = URI.create(Names.RUNTIME_NAME + "/ServletRequestDispatcher");

    /**
     * Initializes the dispatcher.
     *
     * @param config the ServletConfig for the boot web application context
     * @throws ServletException if an error is encountered during initialization
     */
    void init(ServletConfig config) throws ServletException;

    /**
     * Dispatch servlet requests to the Fabric3 runtime
     *
     * @param req the original servlet request
     * @param res the original servlet response
     * @throws ServletException if the request cannot be handled
     * @throws IOException      if an input or output error occurs while handling the request
     */
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

}
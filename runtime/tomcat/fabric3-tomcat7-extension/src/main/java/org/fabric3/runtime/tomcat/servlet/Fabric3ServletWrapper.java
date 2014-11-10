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
import javax.servlet.ServletException;

import org.apache.catalina.core.StandardWrapper;

/**
 * Specialization of the Tomcat <code>StandardWrapper</code> that returns an existing servlet instance as opposed to creating a new one.
 */
public class Fabric3ServletWrapper extends StandardWrapper {
    private Servlet servlet;

    public Fabric3ServletWrapper(Servlet servlet) {
        this.servlet = servlet;
        super.setServletClass(servlet.getClass().getName());
    }

    @Override
    public Servlet loadServlet() throws ServletException {
        return servlet;
    }

    @Override
    public void load() throws ServletException {
        loadServlet();
    }
}


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
package org.fabric3.binding.web.runtime.common;

import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class GatewayServletConfig implements ServletConfig {
    ServletContext context;

    public GatewayServletConfig(ServletContext context) {
        this.context = context;
    }

    public String getServletName() {
        return "AtmosphereServlet";
    }

    public ServletContext getServletContext() {
        return context;
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }

    public Enumeration<String> getInitParameterNames() {
        return context.getInitParameterNames();
    }
}
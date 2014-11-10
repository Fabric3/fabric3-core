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
package org.fabric3.transport.jetty.management;

import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>ServletHandler</code> to provide a custom management view.
 */
@Management
public class ManagedServletHandler extends ServletHandler {
    private static final String[] EMPTY_RESULT = new String[0];

    @ManagementOperation(description = "Servlet path mappings")
    public String[] getServletMappingsInfo() {
        ServletMapping[] mappings = super.getServletMappings();
        if (mappings == null) {
            return EMPTY_RESULT;
        }
        String[] values = new String[mappings.length];
        for (int i = 0, mappingsLength = mappings.length; i < mappingsLength; i++) {
            ServletMapping mapping = mappings[i];
            values[i] = mapping.toString();
        }
        return values;
    }

    @ManagementOperation(description = "Filter mappings")
    public String[] getFilterMappingsInfo() {
        FilterMapping[] mappings = super.getFilterMappings();
        if (mappings == null) {
            return EMPTY_RESULT;
        }
        String[] values = new String[mappings.length];
        for (int i = 0, mappingsLength = mappings.length; i < mappingsLength; i++) {
            FilterMapping mapping = mappings[i];
            values[i] = mapping.toString();
        }
        return values;
    }

    @Override
    @ManagementOperation(description = "True if the handler is available")
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    public void addServlet(ServletHolder holder) {
        super.addServlet(holder);
    }
}
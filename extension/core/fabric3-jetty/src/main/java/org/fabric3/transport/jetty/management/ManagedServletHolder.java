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

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>ServletHolder</code> to provide a custom management view.
 */
@Management
public class ManagedServletHolder extends ServletHolder {

    public ManagedServletHolder() {
    }

    public ManagedServletHolder(Servlet servlet) {
        super(servlet);
    }

    public ManagedServletHolder(Class<? extends Servlet> servlet) {
        super(servlet);
    }

    @Override
    @ManagementOperation(description = "Servlet availability")
    public boolean isAvailable() {
        return super.isAvailable();
    }

    @Override
    @ManagementOperation(description = "Servlet context path")
    public String getContextPath() {
        return super.getContextPath();
    }

    @Override
    @ManagementOperation(description = "Start the servlet from servicing requests")
    public void doStart() throws Exception {
        super.doStart();
    }

    @Override
    @ManagementOperation(description = "Stop the servlet from servicing requests")
    public void doStop() throws Exception {
        super.doStop();
    }
}
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

import org.eclipse.jetty.servlet.ServletHolder;

import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.management.OperationType;

/**
 *
 */
public class ServletManager {
    private ServletHolder holder;

    public ServletManager(ServletHolder holder) {
        this.holder = holder;
    }

    @ManagementOperation (type = OperationType.POST)
    public void start() throws Exception {
        holder.doStart();
    }

    @ManagementOperation (type = OperationType.POST)
    public void stop() throws Exception {
        holder.doStop();
    }

    @ManagementOperation
    public String getState() throws Exception {
        return holder.getState();
    }

    @ManagementOperation
    public boolean isAvailable() throws Exception {
        return holder.isAvailable();
    }

}

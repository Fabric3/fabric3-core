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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.monitor.runtime;

import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.monitor.MonitorProxyService;
import org.fabric3.monitor.provision.MonitorWireTarget;
import org.fabric3.spi.container.builder.TargetWireAttacher;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.oasisopen.sca.annotation.Reference;

/**
 * TargetWireAttacher that handles monitor resources.
 */
public class MonitorWireAttacher implements TargetWireAttacher<MonitorWireTarget> {
    private final MonitorProxyService monitorService;
    private ComponentManager componentManager;

    public MonitorWireAttacher(@Reference MonitorProxyService monitorService, @Reference ComponentManager componentManager) {
        this.monitorService = monitorService;
        this.componentManager = componentManager;
    }

    public Supplier<?> createSupplier(MonitorWireTarget target) throws Fabric3Exception {
        Class<?> type = target.getMonitorType();
        Component monitorable = componentManager.getComponent(target.getMonitorable());
        Object monitor = monitorService.createMonitor(type, monitorable, target.getDestination());
        return () -> monitor;
    }
}

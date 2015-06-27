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
package org.fabric3.runtime.weblogic.monitor;

import java.net.URI;
import java.util.List;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.monitor.MonitorService;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@Management(name = "MonitorService", path = "/runtime/monitor", description = "Sets monitoring levels for the runtime")
public class WebLogicMonitorService implements MonitorService {
    private ComponentManager manager;

    public WebLogicMonitorService(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    @ManagementOperation(description = "Sets the monitoring level for a component")
    public void setComponentLevel(String uri, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        List<Component> components = manager.getComponentsInHierarchy(URI.create(uri));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    public void setProviderLevel(String key, String level) {
        // no-op - not supported
    }

    public MonitorLevel getProviderLevel(String key) {
        return null;
    }

}
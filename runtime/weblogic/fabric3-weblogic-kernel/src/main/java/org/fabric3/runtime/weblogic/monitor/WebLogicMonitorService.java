/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.runtime.weblogic.monitor;


import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.monitor.MonitorService;

/**
 * No-op implementation of MonitorLevelService.
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

    @ManagementOperation(description = "Sets the monitoring level for a deployable composite")
    public void setDeployableLevel(String deployable, String level) {
        MonitorLevel parsed = MonitorLevel.valueOf(level);
        List<Component> components = manager.getDeployedComponents(QName.valueOf(deployable));
        for (Component component : components) {
            component.setLevel(parsed);
        }
    }

    public void setProviderLevel(String key, String level) {
        // no-op - not supported
    }

}
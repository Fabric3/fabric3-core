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
package org.fabric3.monitor.impl.builder;

import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.monitor.spi.destination.MonitorDestinationBuilder;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitor;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDestination;
import org.fabric3.spi.container.builder.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates and registers or unregisters monitor destinations with the destination registry.
 */
@EagerInit
public class MonitorBuilder implements ResourceBuilder<PhysicalMonitor> {
    private Map<Class<?>, MonitorDestinationBuilder<?>> builders;

    @Reference
    public void setBuilders(Map<Class<?>, MonitorDestinationBuilder<?>> builders) {
        this.builders = builders;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalMonitor physicalMonitor) {
        PhysicalMonitorDestination destination = physicalMonitor.getDestination();
        MonitorDestinationBuilder builder = getBuilder(destination);
        builder.build(destination);
    }

    @SuppressWarnings("unchecked")
    public void remove(PhysicalMonitor physicalMonitor) {
        PhysicalMonitorDestination destination = physicalMonitor.getDestination();
        MonitorDestinationBuilder builder = getBuilder(destination);
        builder.remove(destination);
    }

    private MonitorDestinationBuilder getBuilder(PhysicalMonitorDestination destinationDefinition) {
        MonitorDestinationBuilder builder = builders.get(destinationDefinition.getClass());
        if (builder == null) {
            throw new Fabric3Exception("Unknown destination type: " + destinationDefinition.getClass().getName());
        }
        return builder;
    }

}

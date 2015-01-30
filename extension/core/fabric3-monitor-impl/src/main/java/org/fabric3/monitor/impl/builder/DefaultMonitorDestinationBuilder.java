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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.monitor.impl.common.MonitorConstants;
import org.fabric3.monitor.impl.destination.DefaultMonitorDestination;
import org.fabric3.monitor.impl.model.physical.PhysicalDefaultMonitorDestinationDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.monitor.spi.destination.MonitorDestination;
import org.fabric3.monitor.spi.destination.MonitorDestinationBuilder;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.writer.EventWriter;
import org.fabric3.spi.container.ContainerException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates and registers or unregisters default monitor destinations with the destination registry.
 */
@EagerInit
public class DefaultMonitorDestinationBuilder implements MonitorDestinationBuilder<PhysicalDefaultMonitorDestinationDefinition> {
    private MonitorDestinationRegistry registry;
    private EventWriter eventWriter;

    private int capacity = MonitorConstants.DEFAULT_BUFFER_CAPACITY;

    private Map<Class<?>, AppenderBuilder<?>> appenderBuilders;

    @Reference
    public void setAppenderBuilders(Map<Class<?>, AppenderBuilder<?>> appenderBuilders) {
        this.appenderBuilders = appenderBuilders;
    }

    @Property(required = false)
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public DefaultMonitorDestinationBuilder(@Reference MonitorDestinationRegistry registry, @Reference EventWriter eventWriter) {
        this.registry = registry;
        this.eventWriter = eventWriter;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalDefaultMonitorDestinationDefinition definition) throws ContainerException {
        // create the appenders for the destination
        List<Appender> appenders = new ArrayList<>();
        for (PhysicalAppenderDefinition appenderDefinition : definition.getDefinitions()) {
            AppenderBuilder builder = appenderBuilders.get(appenderDefinition.getClass());
            if (builder == null) {
                throw new ContainerException("Unknown appender type: " + definition.getClass());
            }
            Appender appender;
            appender = builder.build(appenderDefinition);
            appenders.add(appender);
        }

        String name = definition.getName();
        MonitorDestination destination = new DefaultMonitorDestination(name, eventWriter, capacity, appenders);
        try {
            destination.start();
        } catch (IOException e) {
            throw new ContainerException(e);
        }
        registry.register(destination);
    }

    public void remove(PhysicalDefaultMonitorDestinationDefinition definition) throws ContainerException {
        MonitorDestination destination = registry.unregister(definition.getName());
        try {
            destination.stop();
        } catch (IOException e) {
            throw new ContainerException(e);
        }

    }

}

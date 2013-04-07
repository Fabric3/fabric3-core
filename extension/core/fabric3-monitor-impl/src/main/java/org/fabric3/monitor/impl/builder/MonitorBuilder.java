/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.monitor.impl.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.fabric3.monitor.impl.destination.MonitorDestination;
import org.fabric3.monitor.impl.destination.MonitorDestinationImpl;
import org.fabric3.monitor.impl.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.impl.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.impl.physical.PhysicalMonitorDefinition;
import org.fabric3.monitor.spi.appender.Appender;
import org.fabric3.monitor.spi.appender.AppenderBuilder;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates and registers or unregisters monitor destinations with the destination registry.
 */
@EagerInit
public class MonitorBuilder implements ResourceBuilder<PhysicalMonitorDefinition> {
    private MonitorDestinationRegistry registry;
    private Map<Class<?>, AppenderBuilder<?>> appenderBuilders;

    @Reference
    public void setAppenderBuilders(Map<Class<?>, AppenderBuilder<?>> appenderBuilders) {
        this.appenderBuilders = appenderBuilders;
    }

    public MonitorBuilder(@Reference MonitorDestinationRegistry registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalMonitorDefinition definition) throws BuilderException {
        // create the appenders for the destination
        List<Appender> appenders = new ArrayList<Appender>();
        for (PhysicalAppenderDefinition appenderDefinition : definition.getDefinitions()) {
            AppenderBuilder builder = appenderBuilders.get(appenderDefinition.getClass());
            if (builder == null) {
                throw new BuilderException("Unknown appender type: " + definition.getClass());
            }
            Appender appender = builder.build(appenderDefinition);
            appenders.add(appender);
        }

        MonitorDestination destination = new MonitorDestinationImpl(definition.getName(), appenders);
        registry.register(destination);
    }

    public void remove(PhysicalMonitorDefinition definition) throws BuilderException {
        registry.unregister(definition.getName());
    }
}

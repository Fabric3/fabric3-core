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

import java.util.Map;

import org.fabric3.monitor.spi.destination.MonitorDestinationBuilder;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDefinition;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDestinationDefinition;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.resource.ResourceBuilder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Instantiates and registers or unregisters monitor destinations with the destination registry.
 */
@EagerInit
public class MonitorBuilder implements ResourceBuilder<PhysicalMonitorDefinition> {
    private Map<Class<?>, MonitorDestinationBuilder<?>> builders;

    @Reference
    public void setBuilders(Map<Class<?>, MonitorDestinationBuilder<?>> builders) {
        this.builders = builders;
    }

    @SuppressWarnings("unchecked")
    public void build(PhysicalMonitorDefinition definition) throws BuilderException {
        PhysicalMonitorDestinationDefinition destinationDefinition = definition.getDestinationDefinition();
        MonitorDestinationBuilder builder = getBuilder(destinationDefinition);
        builder.build(destinationDefinition);
    }

    @SuppressWarnings("unchecked")
    public void remove(PhysicalMonitorDefinition definition) throws BuilderException {
        PhysicalMonitorDestinationDefinition destinationDefinition = definition.getDestinationDefinition();
        MonitorDestinationBuilder builder = getBuilder(destinationDefinition);
        builder.remove(destinationDefinition);
    }

    private MonitorDestinationBuilder getBuilder(PhysicalMonitorDestinationDefinition destinationDefinition) throws BuilderException {
        MonitorDestinationBuilder builder = builders.get(destinationDefinition.getClass());
        if (builder == null) {
            throw new BuilderException("Unknown destination type: " + destinationDefinition.getClass().getName());
        }
        return builder;
    }

}

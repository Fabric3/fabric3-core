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
package org.fabric3.monitor.impl.generator;

import java.util.List;
import java.util.Map;

import org.fabric3.monitor.impl.model.MonitorResourceDefinition;
import org.fabric3.monitor.spi.appender.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDefinition;
import org.fabric3.monitor.spi.appender.AppenderDefinition;
import org.fabric3.monitor.spi.appender.AppenderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates a {@link PhysicalMonitorDefinition} from a {@link MonitorResourceDefinition}.
 */
@EagerInit
public class MonitorResourceGenerator implements ResourceGenerator<MonitorResourceDefinition> {
    private Map<Class<?>, AppenderGenerator<?>> appenderGenerators;

    @Reference
    public void setAppenderGenerators(Map<Class<?>, AppenderGenerator<?>> appenderGenerators) {
        this.appenderGenerators = appenderGenerators;
    }

    @SuppressWarnings("unchecked")
    public PhysicalMonitorDefinition generateResource(LogicalResource<MonitorResourceDefinition> logicalResource) throws GenerationException {
        MonitorResourceDefinition resourceDefinition = logicalResource.getDefinition();

        PhysicalMonitorDefinition physicalDefinition = new PhysicalMonitorDefinition(resourceDefinition.getName());

        List<AppenderDefinition> appenderDefinitions = resourceDefinition.getAppenderDefinitions();

        for (AppenderDefinition definition : appenderDefinitions) {
            AppenderGenerator generator = getAppenderGenerator(definition);
            PhysicalAppenderDefinition physicalAppenderDefinition = generator.generateResource(definition);
            physicalDefinition.add(physicalAppenderDefinition);
        }
        return physicalDefinition;
    }

    private AppenderGenerator getAppenderGenerator(AppenderDefinition definition) throws GenerationException {
        AppenderGenerator generator = appenderGenerators.get(definition.getClass());
        if (generator == null) {
            throw new GenerationException("Unknown appender type: " + definition.getClass());
        }
        return generator;
    }

}


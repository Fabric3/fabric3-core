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

import org.fabric3.monitor.impl.model.physical.PhysicalDefaultMonitorDestinationDefinition;
import org.fabric3.monitor.impl.model.type.DefaultMonitorDestinationDefinition;
import org.fabric3.monitor.spi.appender.AppenderGenerator;
import org.fabric3.monitor.spi.destination.MonitorDestinationGenerator;
import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.model.type.AppenderDefinition;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates {@link PhysicalDefaultMonitorDestinationDefinition}s.
 */
public class DefaultMonitorDestinationGenerator implements MonitorDestinationGenerator<DefaultMonitorDestinationDefinition> {
    private Map<Class<?>, AppenderGenerator<?>> appenderGenerators;

    @Reference
    public void setAppenderGenerators(Map<Class<?>, AppenderGenerator<?>> appenderGenerators) {
        this.appenderGenerators = appenderGenerators;
    }

    @SuppressWarnings("unchecked")
    public PhysicalDefaultMonitorDestinationDefinition generateResource(DefaultMonitorDestinationDefinition definition) throws GenerationException {
        String name = definition.getParent().getName();
        PhysicalDefaultMonitorDestinationDefinition physicalDefinition = new PhysicalDefaultMonitorDestinationDefinition(name);
        List<AppenderDefinition> appenderDefinitions = definition.getAppenderDefinitions();

        for (AppenderDefinition appenderDefinition : appenderDefinitions) {
            AppenderGenerator generator = getAppenderGenerator(appenderDefinition);
            PhysicalAppenderDefinition physicalAppenderDefinition = generator.generateResource(appenderDefinition);
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

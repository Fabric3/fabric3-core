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
package org.fabric3.fabric.domain.generator.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.fabric.container.command.StopContextCommand;
import org.fabric3.api.host.Names;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 *
 */
public class StopContextCommandGeneratorImpl implements StopContextCommandGenerator {

    public Map<String, List<CompensatableCommand>> generate(List<LogicalComponent<?>> components) throws GenerationException {
        Map<String, List<CompensatableCommand>> commands = new HashMap<>();
        for (LogicalComponent<?> component : components) {
            if (component.getState() == LogicalState.MARKED) {
                List<CompensatableCommand> list = getCommands(component.getZone(), commands);
                QName deployable = component.getDeployable();
                // only log application composite deployments
                boolean log = !component.getUri().toString().startsWith(Names.RUNTIME_NAME);
                StopContextCommand command = new StopContextCommand(deployable, log);
                if (!list.contains(command)) {
                    list.add(command);
                }
            }
        }
        return commands;
    }

    private List<CompensatableCommand> getCommands(String zone, Map<String, List<CompensatableCommand>> commands) {
        List<CompensatableCommand> list = commands.get(zone);
        if (list == null) {
            list = new ArrayList<>();
            commands.put(zone, list);
        }
        return list;
    }
}

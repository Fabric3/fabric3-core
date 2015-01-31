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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.context;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.Names;
import org.fabric3.fabric.container.command.StartContextCommand;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Generates commands to start deployable contexts in a zone.
 */
@EagerInit
public class StartContextCommandGeneratorImpl implements StartContextCommandGenerator {

    public List<Command> generate(List<LogicalComponent<?>> components) throws ContainerException {
        List<Command> commands = new ArrayList<>();
        // only log application composite deployments
        components.stream().filter(component -> component.getState() == LogicalState.NEW).forEach(component -> {
            QName deployable = component.getDeployable();
            // only log application composite deployments
            boolean log = !component.getUri().toString().startsWith(Names.RUNTIME_NAME);
            StartContextCommand command = new StartContextCommand(deployable, log);
            if (!commands.contains(command)) {
                commands.add(command);
            }
        });
        return commands;
    }

}

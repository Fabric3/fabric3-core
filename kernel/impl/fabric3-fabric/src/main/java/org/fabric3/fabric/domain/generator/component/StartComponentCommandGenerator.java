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
package org.fabric3.fabric.domain.generator.component;

import java.util.Optional;

import org.fabric3.fabric.container.command.StartComponentCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Creates a command to start an atomic component on a runtime.
 */
public class StartComponentCommandGenerator implements CommandGenerator<StartComponentCommand> {

    public int getOrder() {
        return START_COMPONENTS;
    }

    @SuppressWarnings("unchecked")
    public Optional<StartComponentCommand> generate(LogicalComponent<?> component) {
        // start a component if it is atomic and not provisioned
        if (!(component instanceof LogicalCompositeComponent) && (component.getState() == LogicalState.NEW)) {
            return Optional.of(new StartComponentCommand(component.getUri()));
        }
        return Optional.empty();
    }
}

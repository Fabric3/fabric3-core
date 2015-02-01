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
package org.fabric3.fabric.domain.generator.component;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.fabric.container.command.StopComponentCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Creates a command to stop an atomic component on a runtime.
 */
public class StopComponentCommandGenerator implements CommandGenerator {

    public int getOrder() {
        return PREPARE;
    }

    @SuppressWarnings("unchecked")
    public StopComponentCommand generate(LogicalComponent<?> component) throws Fabric3Exception {
        if (!(component instanceof LogicalCompositeComponent) && component.getState() == LogicalState.MARKED) {
            return new StopComponentCommand(component.getUri());
        }
        return null;
    }
}

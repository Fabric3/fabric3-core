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

import org.fabric3.fabric.container.command.BuildComponentCommand;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates a command to build a component on a runtime.
 */
public class BuildComponentCommandGenerator extends AbstractBuildComponentCommandGenerator {

    public BuildComponentCommandGenerator(@Reference GeneratorRegistry generatorRegistry) {
        super(generatorRegistry);
    }

    public int getOrder() {
        return BUILD_COMPONENTS;
    }

    public BuildComponentCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (!(component instanceof LogicalCompositeComponent) && (component.getState() == LogicalState.NEW || !incremental)) {
            PhysicalComponentDefinition definition = generateDefinition(component);
            return new BuildComponentCommand(definition);
        }
        return null;
    }
}

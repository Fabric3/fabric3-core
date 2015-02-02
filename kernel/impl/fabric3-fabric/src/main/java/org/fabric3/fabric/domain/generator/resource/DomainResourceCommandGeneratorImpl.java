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
package org.fabric3.fabric.domain.generator.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Resource;
import org.fabric3.fabric.container.command.BuildResourcesCommand;
import org.fabric3.fabric.container.command.DisposeResourcesCommand;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class DomainResourceCommandGeneratorImpl implements DomainResourceCommandGenerator {
    private GeneratorRegistry generatorRegistry;

    public DomainResourceCommandGeneratorImpl(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    public Optional<Command> generateBuild(LogicalResource resource) throws Fabric3Exception {
        if (resource.getState() != LogicalState.NEW) {
            return Optional.empty();
        }

        List<PhysicalResourceDefinition> definitions = createDefinitions(resource);
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BuildResourcesCommand(definitions));
    }

    public Optional<Command> generateDispose(LogicalResource resource) throws Fabric3Exception {
        if (resource.getState() != LogicalState.MARKED) {
            return Optional.empty();
        }
        List<PhysicalResourceDefinition> definitions = createDefinitions(resource);
        if (definitions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DisposeResourcesCommand(definitions));
    }

    @SuppressWarnings({"unchecked"})
    private List<PhysicalResourceDefinition> createDefinitions(LogicalResource resource) throws Fabric3Exception {
        List<PhysicalResourceDefinition> definitions = new ArrayList<>();
        Resource resourceDefinition = resource.getDefinition();
        ResourceGenerator generator = generatorRegistry.getResourceGenerator(resourceDefinition.getClass());
        PhysicalResourceDefinition definition = generator.generateResource(resource);
        definitions.add(definition);
        return definitions;
    }

}
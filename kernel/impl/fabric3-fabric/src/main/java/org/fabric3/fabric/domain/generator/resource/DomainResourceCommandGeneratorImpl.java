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

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.container.command.BuildResourcesCommand;
import org.fabric3.fabric.container.command.DisposeResourcesCommand;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.api.model.type.component.ResourceDefinition;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.resource.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;

/**
 *
 */
public class DomainResourceCommandGeneratorImpl implements DomainResourceCommandGenerator {
    private GeneratorRegistry generatorRegistry;

    public DomainResourceCommandGeneratorImpl(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    public CompensatableCommand generateBuild(LogicalResource resource, boolean incremental) throws GenerationException {
        if (resource.getState() != LogicalState.NEW && incremental) {
            return null;
        }

        List<PhysicalResourceDefinition> definitions = createDefinitions(resource);
        if (definitions.isEmpty()) {
            return null;
        }
        return new BuildResourcesCommand(definitions);
    }

    public CompensatableCommand generateDispose(LogicalResource resource, boolean incremental) throws GenerationException {
        if (resource.getState() != LogicalState.MARKED) {
            return null;
        }
        List<PhysicalResourceDefinition> definitions = createDefinitions(resource);
        if (definitions.isEmpty()) {
            return null;
        }
        return new DisposeResourcesCommand(definitions);
    }

    @SuppressWarnings({"unchecked"})
    private List<PhysicalResourceDefinition> createDefinitions(LogicalResource resource) throws GenerationException {
        List<PhysicalResourceDefinition> definitions = new ArrayList<>();
        ResourceDefinition resourceDefinition = resource.getDefinition();
        ResourceGenerator generator = generatorRegistry.getResourceGenerator(resourceDefinition.getClass());
        PhysicalResourceDefinition definition = generator.generateResource(resource);
        definitions.add(definition);
        return definitions;
    }


}
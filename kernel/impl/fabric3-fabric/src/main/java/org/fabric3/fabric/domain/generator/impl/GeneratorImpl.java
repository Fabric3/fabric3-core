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
package org.fabric3.fabric.domain.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.fabric.domain.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.domain.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.domain.generator.resource.DomainResourceCommandGenerator;
import org.fabric3.spi.container.command.Command;
import org.fabric3.spi.domain.generator.Deployment;
import org.fabric3.spi.domain.generator.Generator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.type.remote.RemoteImplementation;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default Generator implementation.
 */
@EagerInit
public class GeneratorImpl implements Generator {
    private List<CommandGenerator> commandGenerators;
    private StartContextCommandGenerator startContextCommandGenerator;
    private StopContextCommandGenerator stopContextCommandGenerator;

    /**
     * Injected after bootstrap.
     */
    @Reference
    private DomainResourceCommandGenerator resourceGenerator;

    @Constructor
    public GeneratorImpl(@Reference List<CommandGenerator> commandGenerators,
                         @Reference StartContextCommandGenerator startContextCommandGenerator,
                         @Reference StopContextCommandGenerator stopContextCommandGenerator) {
        this.startContextCommandGenerator = startContextCommandGenerator;
        this.stopContextCommandGenerator = stopContextCommandGenerator;
        // sort the command generators
        this.commandGenerators = sortGenerators(commandGenerators);
    }

    public Deployment generate(LogicalCompositeComponent domain) throws Fabric3Exception {

        List<LogicalComponent<?>> components = domain.getComponents().stream().collect(Collectors.toList());

        Deployment deployment = new Deployment();

        // generate stop context information
        List<Command> stopCommands = stopContextCommandGenerator.generate(components);
        deployment.addCommands(stopCommands);

        // generate commands for domain-level resources being deployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                Optional<Command> command = resourceGenerator.generateBuild(resource);
                command.ifPresent(deployment::addCommand);
            }
        }

        for (CommandGenerator<?> generator : commandGenerators) {
            for (LogicalComponent<?> component : components) {
                if (component.getDefinition().getImplementation() instanceof RemoteImplementation) {
                    continue;
                }
                Optional<? extends Command> command = generator.generate(component);
                command.ifPresent(generated -> {
                    if (!deployment.getCommands().contains(generated)) {
                        deployment.addCommand(generated);
                    }
                });

            }
        }

        // generate commands for domain-level resources being undeployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                Optional<Command> command = resourceGenerator.generateDispose(resource);
                command.ifPresent(deployment::addCommand);
            }
        }
        // start contexts
        List<Command> startCommands = startContextCommandGenerator.generate(components);
        deployment.addCommands(startCommands);

        return deployment;
    }

    private List<CommandGenerator> sortGenerators(List<? extends CommandGenerator> commandGenerators) {
        Comparator<CommandGenerator> generatorComparator = (first, second) -> first.getOrder() - second.getOrder();
        List<CommandGenerator> sorted = new ArrayList<>(commandGenerators);
        Collections.sort(sorted, generatorComparator);
        return sorted;
    }

}

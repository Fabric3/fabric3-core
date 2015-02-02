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
    private static final Comparator<LogicalComponent<?>> COMPARATOR = (first, second) -> first.getUri().compareTo(second.getUri());

    private List<CommandGenerator> commandGenerators;
    private StartContextCommandGenerator startContextCommandGenerator;
    private StopContextCommandGenerator stopContextCommandGenerator;
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

    /**
     * Injected after bootstrap.
     *
     * @param generator the resource generator
     */
    @Reference
    public void setResourceGenerator(DomainResourceCommandGenerator generator) {
        this.resourceGenerator = generator;
    }

    public Deployment generate(LogicalCompositeComponent domain) throws Fabric3Exception {

        List<LogicalComponent<?>> sorted = topologicalSort(domain);

        Deployment deployment = new Deployment();

        // generate stop context information
        List<Command> stopCommands = stopContextCommandGenerator.generate(sorted);
        deployment.addCommands(stopCommands);

        // generate commands for domain-level resources being deployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                Command command = resourceGenerator.generateBuild(resource);
                if (command != null) {
                    deployment.addCommand(command);
                }
            }
        }

        for (CommandGenerator generator : commandGenerators) {
            for (LogicalComponent<?> component : sorted) {
                if (component.getDefinition().getImplementation() instanceof RemoteImplementation) {
                    continue;
                }
                Command command = generator.generate(component);
                if (command != null) {
                    if (deployment.getCommands().contains(command)) {
                        continue;
                    }
                    deployment.addCommand(command);
                }
            }
        }

        // generate commands for domain-level resources being undeployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                Command command = resourceGenerator.generateDispose(resource);
                if (command != null) {
                    deployment.addCommand(command);
                }
            }
        }
        // start contexts
        List<Command> startCommands = startContextCommandGenerator.generate(sorted);
        deployment.addCommands(startCommands);

        return deployment;
    }

    /**
     * Topologically sorts components in the domain according to their URI.
     *
     * @param domain the domain composite
     * @return a sorted collection
     */
    private List<LogicalComponent<?>> topologicalSort(LogicalCompositeComponent domain) {
        List<LogicalComponent<?>> sorted = domain.getComponents().stream().collect(Collectors.toList());
        //            if (component instanceof LogicalCompositeComponent) {
        //                flatten((LogicalCompositeComponent) component, sorted);
        //            }
        Collections.sort(sorted, COMPARATOR);
        return sorted;
    }

//    /**
//     * Recursively adds composite children to the collection of components
//     *
//     * @param component  the top-level composite component
//     * @param components the collection
//     */
//    private void flatten(LogicalCompositeComponent component, List<LogicalComponent<?>> components) {
//        for (LogicalComponent<?> child : component.getComponents()) {
//            components.add(child);
//            if (child instanceof LogicalCompositeComponent) {
//                flatten((LogicalCompositeComponent) child, components);
//            }
//        }
//    }

    private List<CommandGenerator> sortGenerators(List<? extends CommandGenerator> commandGenerators) {
        Comparator<CommandGenerator> generatorComparator = (first, second) -> first.getOrder() - second.getOrder();
        List<CommandGenerator> sorted = new ArrayList<>(commandGenerators);
        Collections.sort(sorted, generatorComparator);
        return sorted;
    }

}

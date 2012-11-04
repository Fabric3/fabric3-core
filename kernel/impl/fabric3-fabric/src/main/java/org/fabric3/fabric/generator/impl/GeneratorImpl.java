/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GenerationType;
import org.fabric3.fabric.generator.channel.DomainChannelCommandGenerator;
import org.fabric3.fabric.generator.classloader.ClassLoaderCommandGenerator;
import org.fabric3.fabric.generator.collator.ContributionCollator;
import org.fabric3.fabric.generator.context.StartContextCommandGenerator;
import org.fabric3.fabric.generator.context.StopContextCommandGenerator;
import org.fabric3.fabric.generator.extension.ExtensionGenerator;
import org.fabric3.fabric.generator.resource.DomainResourceCommandGenerator;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.generator.Deployment;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.Generator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * Default Generator implementation.
 */
@EagerInit
public class GeneratorImpl implements Generator {
    private static final Comparator<LogicalComponent<?>> COMPARATOR = new Comparator<LogicalComponent<?>>() {
        public int compare(LogicalComponent<?> first, LogicalComponent<?> second) {
            return first.getUri().compareTo(second.getUri());
        }
    };

    private List<CommandGenerator> commandGenerators;
    private ContributionCollator collator;
    private ClassLoaderCommandGenerator classLoaderCommandGenerator;
    private DomainChannelCommandGenerator channelGenerator;
    private StartContextCommandGenerator startContextCommandGenerator;
    private StopContextCommandGenerator stopContextCommandGenerator;
    private ExtensionGenerator extensionGenerator;
    private DomainResourceCommandGenerator resourceGenerator;

    @Constructor
    public GeneratorImpl(@Reference List<CommandGenerator> commandGenerators,
                         @Reference ContributionCollator collator,
                         @Reference ClassLoaderCommandGenerator classLoaderCommandGenerator,
                         @Reference DomainChannelCommandGenerator channelGenerator,
                         @Reference StartContextCommandGenerator startContextCommandGenerator,
                         @Reference StopContextCommandGenerator stopContextCommandGenerator) {
        this.collator = collator;
        this.classLoaderCommandGenerator = classLoaderCommandGenerator;
        this.channelGenerator = channelGenerator;
        this.startContextCommandGenerator = startContextCommandGenerator;
        this.stopContextCommandGenerator = stopContextCommandGenerator;
        // sort the command generators
        this.commandGenerators = sortGenerators(commandGenerators);
    }

    /**
     * Lazily injected after bootstrap.
     *
     * @param extensionGenerator the extension  generator
     */
    @Reference(required = false)
    public void setExtensionGenerator(ExtensionGenerator extensionGenerator) {
        this.extensionGenerator = extensionGenerator;
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

    public Deployment generate(LogicalCompositeComponent domain, boolean incremental) throws GenerationException {

        List<LogicalComponent<?>> sorted = topologicalSort(domain);

        String id = UUID.randomUUID().toString();
        Deployment deployment = new Deployment(id);

        Map<String, List<Contribution>> deployingContributions = generateClassLoaders(deployment, sorted, incremental);
        // generate stop context information
        Map<String, List<CompensatableCommand>> stopCommands = stopContextCommandGenerator.generate(sorted);
        for (Map.Entry<String, List<CompensatableCommand>> entry : stopCommands.entrySet()) {
            deployment.addCommands(entry.getKey(), entry.getValue());
        }

        // generate commands for domain-level resources being deployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                String zone = resource.getZone();
                CompensatableCommand command = resourceGenerator.generateBuild(resource, incremental);
                if (command != null) {
                    deployment.addCommand(zone, command);
                }
            }
        }

        // generate commands for domain-level channels being deployed
        for (LogicalChannel channel : domain.getChannels()) {
            String zone = channel.getZone();
            CompensatableCommand command = channelGenerator.generateBuild(channel, incremental);
            if (command != null) {
                deployment.addCommand(zone, command);
            }
        }

        for (CommandGenerator generator : commandGenerators) {
            for (LogicalComponent<?> component : sorted) {
                CompensatableCommand command = generator.generate(component, incremental);
                if (command != null) {
                    String zone = component.getZone();
                    if (deployment.getDeploymentUnit(zone).getCommands().contains(command)) {
                        continue;
                    }
                    deployment.addCommand(zone, command);
                }
            }
        }

        // generate commands for domain-level channels being undeployed
        for (LogicalChannel channel : domain.getChannels()) {
            String zone = channel.getZone();
            CompensatableCommand command = channelGenerator.generateDispose(channel, incremental);
            if (command != null) {
                deployment.addCommand(zone, command);
            }
        }

        // generate commands for domain-level resources being undeployed
        if (resourceGenerator != null) {
            for (LogicalResource<?> resource : domain.getResources()) {
                String zone = resource.getZone();
                CompensatableCommand command = resourceGenerator.generateDispose(resource, incremental);
                if (command != null) {
                    deployment.addCommand(zone, command);
                }
            }
        }
        // start contexts
        Map<String, List<CompensatableCommand>> startCommands = startContextCommandGenerator.generate(sorted, incremental);
        for (Map.Entry<String, List<CompensatableCommand>> entry : startCommands.entrySet()) {
            deployment.addCommands(entry.getKey(), entry.getValue());
        }

        // release classloaders for components being undeployed that are no longer referenced if the deployment is remote
        generateReleaseClassLoaders(deployment, sorted, deployingContributions, incremental);
        return deployment;
    }

    /**
     * Generates classloader provision commands.
     *
     * @param deployment  the map of commands for deployment
     * @param components  the components being deployed
     * @param incremental the type of generation being performed
     * @return the contributions being deployed
     * @throws GenerationException if an error during generation is encountered
     */
    private Map<String, List<Contribution>> generateClassLoaders(Deployment deployment, List<LogicalComponent<?>> components, boolean incremental)
            throws GenerationException {
        Map<String, List<Contribution>> deployingContributions;
        if (incremental) {
            deployingContributions = collator.collateContributions(components, GenerationType.INCREMENTAL);
        } else {
            deployingContributions = collator.collateContributions(components, GenerationType.FULL);
        }

        Map<String, List<CompensatableCommand>> commandsPerZone = classLoaderCommandGenerator.generate(deployingContributions);
        for (Map.Entry<String, List<CompensatableCommand>> entry : commandsPerZone.entrySet()) {
            deployment.addProvisionCommands(entry.getKey(), entry.getValue());
        }
        return deployingContributions;
    }

    /**
     * Generates classloader release commands.
     *
     * @param deployment    the map of commands for deployment
     * @param components    the components being deployed
     * @param contributions the contributions being deployed
     * @param incremental   the type of generation being performed
     * @throws GenerationException if an error during generation is encountered
     */
    private void generateReleaseClassLoaders(Deployment deployment,
                                             List<LogicalComponent<?>> components,
                                             Map<String, List<Contribution>> contributions,
                                             boolean incremental) throws GenerationException {
        Map<String, List<Contribution>> undeployingContributions = collator.collateContributions(components, GenerationType.UNDEPLOY);
        Map<String, List<CompensatableCommand>> releaseCommandsPerZone = classLoaderCommandGenerator.release(undeployingContributions);
        for (Map.Entry<String, List<CompensatableCommand>> entry : releaseCommandsPerZone.entrySet()) {
            deployment.addCommands(entry.getKey(), entry.getValue());
        }

        // generate extension provision commands - this must be done after policies are calculated for policy extensions to be included
        if (incremental) {
            generateExtensionCommands(deployment, contributions, components, GenerationType.INCREMENTAL);
        } else {
            generateExtensionCommands(deployment, contributions, components, GenerationType.FULL);
        }
        // release extensions that are no longer used
        generateExtensionCommands(deployment, undeployingContributions, components, GenerationType.UNDEPLOY);
    }

    /**
     * Generate extension provision commands for the contributions and components being deployed/undeployed
     *
     * @param deployment             the map of commands for deployment
     * @param deployingContributions the contributions being deployed
     * @param components             the components being deployed
     * @param type                   the type of generation being performed
     * @throws GenerationException if an error during generation is encountered
     */
    private void generateExtensionCommands(Deployment deployment,
                                           Map<String, List<Contribution>> deployingContributions,
                                           List<LogicalComponent<?>> components,
                                           GenerationType type) throws GenerationException {
        if (extensionGenerator != null) {
            Map<String, List<CompensatableCommand>> deploymentCmds = deployment.getCommands();
            Map<String, CompensatableCommand> zoneExtensions = extensionGenerator.generate(deployingContributions, components, deploymentCmds, type);
            if (zoneExtensions != null) {
                for (Map.Entry<String, CompensatableCommand> entry : zoneExtensions.entrySet()) {
                    if (type == GenerationType.UNDEPLOY) {
                        deployment.addCommand(entry.getKey(), entry.getValue());
                    } else {
                        // if an extension is being provisioned, the command needs to be executed before others
                        deployment.addExtensionCommand(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * Topologically sorts components in the domain according to their URI.
     *
     * @param domain the domain composite
     * @return a sorted collection
     */
    private List<LogicalComponent<?>> topologicalSort(LogicalCompositeComponent domain) {
        List<LogicalComponent<?>> sorted = new ArrayList<LogicalComponent<?>>();
        for (LogicalComponent<?> component : domain.getComponents()) {
            sorted.add(component);
            if (component instanceof LogicalCompositeComponent) {
                flatten((LogicalCompositeComponent) component, sorted);
            }
        }
        Collections.sort(sorted, COMPARATOR);
        return sorted;
    }

    /**
     * Recursively adds composite children to the collection of components
     *
     * @param component  the top-level composite component
     * @param components the collection
     */
    private void flatten(LogicalCompositeComponent component, List<LogicalComponent<?>> components) {
        for (LogicalComponent<?> child : component.getComponents()) {
            components.add(child);
            if (child instanceof LogicalCompositeComponent) {
                flatten((LogicalCompositeComponent) child, components);
            }
        }
    }

    private List<CommandGenerator> sortGenerators(List<? extends CommandGenerator> commandGenerators) {
        Comparator<CommandGenerator> generatorComparator = new Comparator<CommandGenerator>() {

            public int compare(CommandGenerator first, CommandGenerator second) {
                return first.getOrder() - second.getOrder();
            }
        };
        List<CommandGenerator> sorted = new ArrayList<CommandGenerator>(commandGenerators);
        Collections.sort(sorted, generatorComparator);
        return sorted;
    }


}

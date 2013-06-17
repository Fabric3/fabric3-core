/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
*/
package org.fabric3.fabric.generator.extension;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.command.AbstractExtensionsCommand;
import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.fabric.command.UnProvisionExtensionsCommand;
import org.fabric3.fabric.command.WireCommand;
import org.fabric3.fabric.generator.GenerationType;
import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * Default ExtensionGenerator implementation.
 */
public class ExtensionGeneratorImpl implements ExtensionGenerator {
    private MetaDataStore store;
    private HostInfo info;

    public ExtensionGeneratorImpl(@Reference MetaDataStore store, @Reference HostInfo info) {
        this.store = store;
        this.info = info;
    }

    public Map<String, CompensatableCommand> generate(Map<String, List<Contribution>> contributions,
                                                      List<LogicalComponent<?>> components,
                                                      Map<String, List<CompensatableCommand>> deploymentCommands,
                                                      GenerationType type) throws GenerationException {
        if (RuntimeMode.CONTROLLER != info.getRuntimeMode()) {
            // short circuit this unless running in distributed mode
            return null;
        }

        Map<String, CompensatableCommand> commands = new HashMap<String, CompensatableCommand>();

        // evaluate contributions being provisioned for required capabilities
        evaluateContributions(contributions, commands, type);
        // evaluate components for required capabilities
        evaluateComponents(components, commands, type);
        // evaluate policies on wires
        evaluatePolicies(commands, contributions, deploymentCommands, type);

        if (commands.isEmpty()) {
            return null;
        }
        return commands;
    }

    /**
     * Evaluates contributions for required capabilities, resolving those capabilities to extensions.
     *
     * @param contributions the contributions  to evaluate
     * @param commands      the list of commands to update with un/provision extension commands
     * @param type          the generation type
     */
    private void evaluateContributions(Map<String, List<Contribution>> contributions,
                                       Map<String, CompensatableCommand> commands,
                                       GenerationType type) {
        for (Map.Entry<String, List<Contribution>> entry : contributions.entrySet()) {
            String zone = entry.getKey();
            if (LogicalComponent.LOCAL_ZONE.equals(zone)) {
                // skip local runtime
                continue;
            }
            AbstractExtensionsCommand command;
            if (type == GenerationType.UNDEPLOY) {
                command = new UnProvisionExtensionsCommand();
            } else {
                command = new ProvisionExtensionsCommand();
            }

            List<Contribution> zoneContributions = entry.getValue();
            Set<Contribution> extensions = new HashSet<Contribution>();
            for (Contribution contribution : zoneContributions) {
                Set<Contribution> required = store.resolveCapabilities(contribution);
                extensions.addAll(required);
            }
            for (Contribution extension : extensions) {
                URI uri = extension.getUri();
                command.addExtensionUri(uri);
            }
            if (!command.getExtensionUris().isEmpty()) {
                commands.put(zone, command);
            }
        }
    }

    /**
     * Evaluates components for required capabilities, resolving those capabilities to extensions.
     *
     * @param components the components  to evaluate
     * @param commands   the list of commands to update with un/provision extension commands
     * @param type       the generation  type
     */
    private void evaluateComponents(List<LogicalComponent<?>> components,
                                    Map<String, CompensatableCommand> commands,
                                    GenerationType type) {
        for (LogicalComponent<?> component : components) {
            String zone = component.getZone();
            if (LogicalComponent.LOCAL_ZONE.equals(zone)) {
                // skip local runtime
                continue;
            }
            AbstractExtensionsCommand command = getExtensionsCommand(commands, zone, type);
            evaluateComponent(component, command, type);
            if (!command.getExtensionUris().isEmpty()) {
                commands.put(zone, command);
            }
        }
    }

    /**
     * Evaluates a component for required capabilities.
     *
     * @param component the component
     * @param command   the command to update
     * @param type      the generation type
     */
    private void evaluateComponent(LogicalComponent<?> component, AbstractExtensionsCommand command, GenerationType type) {
        Implementation<?> impl = component.getDefinition().getImplementation();
        ComponentType componentType = impl.getComponentType();
        Set<Contribution> extensions = new HashSet<Contribution>();
        if (isGenerate(component.getState(), type)) {
            for (String capability : componentType.getRequiredCapabilities()) {
                extensions.addAll(store.resolveCapability(capability));
            }
            for (String capability : impl.getRequiredCapabilities()) {
                extensions.addAll(store.resolveCapability(capability));
            }
        }
        // evaluate services
        for (LogicalService service : component.getServices()) {
            for (LogicalBinding<?> binding : service.getBindings()) {
                if (isGenerate(binding.getState(), type)) {
                    for (String capability : binding.getDefinition().getRequiredCapabilities()) {
                        extensions.addAll(store.resolveCapability(capability));
                    }
                }
            }
        }
        // evaluate references
        for (LogicalReference reference : component.getReferences()) {
            for (LogicalBinding<?> binding : reference.getBindings()) {
                if (isGenerate(binding.getState(), type)) {
                    for (String capability : binding.getDefinition().getRequiredCapabilities()) {
                        extensions.addAll(store.resolveCapability(capability));
                    }
                }
            }
        }
        for (Contribution extension : extensions) {
            URI uri = extension.getUri();
            if (!command.getExtensionUris().contains(uri) && !Names.HOST_CONTRIBUTION.equals(uri) && !Names.BOOT_CONTRIBUTION.equals(uri)) {
                command.addExtensionUri(uri);
            }
        }
    }

    private boolean isGenerate(LogicalState state, GenerationType type) {
        if (GenerationType.FULL == type && LogicalState.MARKED != state) {
            return true;
        } else if (GenerationType.INCREMENTAL == type && LogicalState.NEW == state) {
            return true;
        } else if (GenerationType.UNDEPLOY == type && LogicalState.MARKED == state) {
            return true;
        }
        return false;
    }

    /**
     * Evaluates policy interceptors added to wires for required capabilities, resolving those capabilities to extensions.
     *
     * @param contributions      the contributions  to evaluate
     * @param deploymentCommands the current deployment commands to introspect for policy interceptors
     * @param type               the generation type
     * @param commands           the list of commands to update with un/provision extension commands
     * @throws GenerationException if an exception occurs
     */
    private void evaluatePolicies(Map<String, CompensatableCommand> commands,
                                  Map<String, List<Contribution>> contributions,
                                  Map<String, List<CompensatableCommand>> deploymentCommands,
                                  GenerationType type) throws GenerationException {
        for (Map.Entry<String, List<CompensatableCommand>> entry : deploymentCommands.entrySet()) {
            String zone = entry.getKey();
            if (LogicalComponent.LOCAL_ZONE.equals(zone)) {
                // skip local runtime
                continue;
            }

            for (Command generatedCommand : entry.getValue()) {
                if (generatedCommand instanceof ConnectionCommand) {
                    ConnectionCommand connectionCommand = (ConnectionCommand) generatedCommand;
                    if (GenerationType.UNDEPLOY == type) {
                        // undeployment, evaluate wire detach commands
                        for (DetachWireCommand command : connectionCommand.getDetachCommands()) {
                            evaluateWireCommand(command, commands, contributions, zone, type);
                        }
                    } else {
                        // a deployment, evaluate the wire attach commands
                        for (AttachWireCommand command : connectionCommand.getAttachCommands()) {
                            evaluateWireCommand(command, commands, contributions, zone, type);
                        }
                    }

                }
            }
        }
    }

    private void evaluateWireCommand(WireCommand wireCommand,
                                     Map<String, CompensatableCommand> commands,
                                     Map<String, List<Contribution>> contributions,
                                     String zone, GenerationType type) throws GenerationException {
        for (PhysicalOperationDefinition operation : wireCommand.getPhysicalWireDefinition().getOperations()) {
            for (PhysicalInterceptorDefinition interceptor : operation.getInterceptors()) {
                URI contributionUri = interceptor.getPolicyClassLoaderId();
                Contribution contribution = store.find(contributionUri);
                if (findContribution(contribution, contributions)) {
                    // the interceptor is bundled with user contribution so skip
                    continue;
                }
                AbstractExtensionsCommand command = getExtensionsCommand(commands, zone, type);
                if (!command.getExtensionUris().contains(contributionUri)
                        && !Names.HOST_CONTRIBUTION.equals(contributionUri)
                        && !Names.BOOT_CONTRIBUTION.equals(contributionUri)) {
                    command.addExtensionUri(contributionUri);
                }
                commands.put(zone, command);
                addDependencies(contribution, command);
            }
        }
    }

    /**
     * Finds a contribution in the list of contributions.
     *
     * @param contribution  the contribution to find
     * @param contributions the contribution to search
     * @return true if found
     */
    private boolean findContribution(Contribution contribution, Map<String, List<Contribution>> contributions) {
        for (List<Contribution> list : contributions.values()) {
            if (list.contains(contribution)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Transitively calculates imported contributions and required capabilities. These are then added to the extension un/provision command.
     *
     * @param contribution the contribution to calculate imports for
     * @param command      the command to update
     * @throws GenerationException if an exception occurs
     */
    private void addDependencies(Contribution contribution, AbstractExtensionsCommand command) throws GenerationException {
        List<ContributionWire<?, ?>> contributionWires = contribution.getWires();
        for (ContributionWire<?, ?> wire : contributionWires) {
            URI uri = wire.getExportContributionUri();
            Contribution imported = store.find(uri);
            addDependencies(imported, command);
            if (!command.getExtensionUris().contains(uri)
                    && !Names.HOST_CONTRIBUTION.equals(uri)
                    && !Names.BOOT_CONTRIBUTION.equals(uri)) {
                command.addExtensionUri(uri);
            }
        }
        Set<Contribution> capabilities = store.resolveCapabilities(contribution);
        for (Contribution capability : capabilities) {
            URI uri = capability.getUri();
            if (!command.getExtensionUris().contains(uri)) {
                command.addExtensionUri(uri);
            }
        }

    }

    /**
     * Gets or creates un/provision extension commands from the command map.
     *
     * @param commands the command map
     * @param zone     the zone extensions are provisioned to
     * @param type     the generation type
     * @return the command
     */
    private AbstractExtensionsCommand getExtensionsCommand(Map<String, CompensatableCommand> commands, String zone, GenerationType type) {
        AbstractExtensionsCommand command;
        command = (AbstractExtensionsCommand) commands.get(zone);    // safe cast
        if (command == null) {
            if (GenerationType.UNDEPLOY == type) {
                command = new UnProvisionExtensionsCommand();
            } else {
                command = new ProvisionExtensionsCommand();
            }

        }
        return command;
    }
}

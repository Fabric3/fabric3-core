/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.fabric.generator.classloader;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.DependencyException;
import org.fabric3.contribution.DependencyService;
import org.fabric3.fabric.command.AttachExtensionCommand;
import org.fabric3.fabric.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.command.UnprovisionClassloaderCommand;
import org.fabric3.host.Names;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.generator.ClassLoaderWireGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * Fabric3 enforces a modular environment where isolation is maintained between user and extension contributions.
 * <p/>
 * Isolation is achieved through a peer-classloader architecture. When contributions are installed, they are loaded in their own classloader, which is
 * given a URI matching the contribution. Imported artifacts will be resolved to contributions that export them based on the specific import/export
 * semantics. The exporting contribution classloaders will be set as a parent of the importing contribution. A contribution that imports artifacts
 * from multiple exporting contributions will have multiple parents.
 * <p/>
 * When a composite is deployed to a zone in multi-VM environments that supports isolation, contributions required to run it (i.e. the contribution
 * containing the composite and any resolved exporting contributions) will be provisioned to runtimes in that zone. The provisioned contributions will
 * be loaded in classloaders which are given a matching URI. Component implementation instances will then be instantiated on runtimes in the
 * appropriate contribution classloader to service requests.
 * <p/>
 * In single-VM environments that support isolation, an optimization is made where classloader provisioning is short-circuited. The same classloader
 * used to install a contribution is reused to instantiate component implementation instances.
 * <p/>
 * In environments that do not support isolation, the creation of individual classloaders is ignored and the host classloader is used for all
 * instantiations.
 * <p/>
 * During undeployment, the process is reversed. Commands for releasing contribution classloaders are sent to the zones where components are being
 * undeployed. Individual zones and runtimes are responsible for deciding when to dispose of classloaders. For example, a contribution classloader
 * used by two composites that is released when one composite is undeployed will not be removed until both composites are undeployed.
 */
public class ClassLoaderCommandGeneratorImpl implements ClassLoaderCommandGenerator {
    private Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators;
    private DependencyService dependencyService;

    public ClassLoaderCommandGeneratorImpl(@Reference Map<Class<? extends ContributionWire<?, ?>>, ClassLoaderWireGenerator<?>> generators) {
        this.generators = generators;
    }

    /**
     * Setter to allow lazy injection of the dependency service. This is used for undeployment only, which is not required during bootstrap.
     *
     * @param dependencyService the dependency service
     */
    @Reference(required = false)
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public Map<String, List<CompensatableCommand>> generate(Map<String, List<Contribution>> contributions) throws GenerationException {
        // commands mapped to zone

        // Create the classloader definitions for contributions required to run the components being deployed.
        // These are created first since they must be instantiated on a runtime prior to component classloaders
        Map<String, List<PhysicalClassLoaderDefinition>> definitionsPerZone = createContributionDefinitions(contributions);
        Map<String, List<CompensatableCommand>> commands = createProvisionCommands(definitionsPerZone);
        createExtensionCommands(commands, contributions);
        return commands;
    }

    public Map<String, List<CompensatableCommand>> release(Map<String, List<Contribution>> contributions) throws GenerationException {
        // commands mapped to the zone
        Map<String, List<CompensatableCommand>> commandsPerZone = new HashMap<String, List<CompensatableCommand>>();

        // generate commands to unprovision contribution classloaders
        for (Map.Entry<String, List<Contribution>> entry : contributions.entrySet()) {
            if (entry.getKey() == null) {
                // Don't unprovision the contribution classloader for locally deployed components since it is shared by the contribution service
                // In a multi-VM domain, the contribution classloaders are unprovisioned when they are no longer referenced by component classloaders.
                // However, in a single-VM domain, the contribution classloader is used by runtime components.
                // Consequently, the contribution classloader cannot be removed until the contribution is uninstalled.
                continue;
            }
            List<CompensatableCommand> commands = commandsPerZone.get(entry.getKey());
            if (commands == null) {
                commands = new ArrayList<CompensatableCommand>();
                commandsPerZone.put(entry.getKey(), commands);
            }
            List<Contribution> ordered;
            try {
                // Contribution classloaders must be removed in reverse order of their dependencies.
                // Order the contributions by dependencies and reverse it to determine the sequence the classloaders must be removed in
                // Ordering is important for classloaders to be properly disposed. Runtimes will only dispose classloaders when they are no longer
                // referenced by other registered classloaders. This requires dependent classloaders to be released first.
                ordered = dependencyService.order(new ArrayList<Contribution>(entry.getValue()));
                Collections.reverse(ordered);
            } catch (DependencyException e) {
                throw new GenerationException(e);
            }
            for (Contribution contribution : ordered) {
                PhysicalClassLoaderDefinition definition = createClassLoaderDefinition(contribution);
                UnprovisionClassloaderCommand command = new UnprovisionClassloaderCommand(definition);
                commands.add(command);
            }
        }

        return commandsPerZone;
    }

    /**
     * Creates classloader definitions for a set of contributions grouped by zone id
     *
     * @param contributionsPerZone the contributions grouped by zone id
     * @return the PhysicalClassLoaderDefinitions grouped by zone
     */
    private Map<String, List<PhysicalClassLoaderDefinition>> createContributionDefinitions(Map<String, List<Contribution>> contributionsPerZone) {
        Map<String, List<PhysicalClassLoaderDefinition>> definitionsPerZone = new HashMap<String, List<PhysicalClassLoaderDefinition>>();
        for (Map.Entry<String, List<Contribution>> entry : contributionsPerZone.entrySet()) {
            String zone = entry.getKey();
            for (Contribution contribution : entry.getValue()) {
                PhysicalClassLoaderDefinition definition = createClassLoaderDefinition(contribution);
                if (definition == null) {
                    continue;
                }
                List<PhysicalClassLoaderDefinition> definitions = definitionsPerZone.get(zone);
                if (definitions == null) {
                    definitions = new ArrayList<PhysicalClassLoaderDefinition>();
                    definitionsPerZone.put(zone, definitions);
                }
                definitions.add(definition);
            }

        }
        return definitionsPerZone;
    }

    private PhysicalClassLoaderDefinition createClassLoaderDefinition(Contribution contribution) {
        URI uri = contribution.getUri();
        if (Names.BOOT_CONTRIBUTION.equals(uri) || Names.HOST_CONTRIBUTION.equals(uri)) {
            return null;
        }
        boolean provisionArtifact = contribution.getLocation() != null;  // don't provision synthetic artifacts;
        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri, provisionArtifact);
        List<ContributionWire<?, ?>> contributionWires = contribution.getWires();
        for (ContributionWire<?, ?> wire : contributionWires) {
            ClassLoaderWireGenerator generator = generators.get(wire.getClass());
            if (generator == null) {
                // not all contribution wires resolve resources through classloaders, so skip if one is not found
                continue;
            }
            PhysicalClassLoaderWireDefinition wireDefinition = generator.generate(wire);
            definition.add(wireDefinition);
        }
        return definition;
    }

    /**
     * Creates provision commands for a set of classloader definitions.
     *
     * @param definitionsPerZone the classloader definitions keyed by zone
     * @return the set of commands keyed by zone
     */
    private Map<String, List<CompensatableCommand>> createProvisionCommands(Map<String, List<PhysicalClassLoaderDefinition>> definitionsPerZone) {
        Map<String, List<CompensatableCommand>> commandsPerZone = new HashMap<String, List<CompensatableCommand>>();
        for (Map.Entry<String, List<PhysicalClassLoaderDefinition>> entry : definitionsPerZone.entrySet()) {
            List<PhysicalClassLoaderDefinition> definitions = entry.getValue();
            List<CompensatableCommand> commands = new ArrayList<CompensatableCommand>();
            commandsPerZone.put(entry.getKey(), commands);
            for (PhysicalClassLoaderDefinition definition : definitions) {
                commands.add(new ProvisionClassloaderCommand(definition));
            }
        }
        return commandsPerZone;
    }

    /**
     * Creates classloader extension attachment commands. Extensions are used to allow contributions to dynamically load classes via reflection from
     * other contribution classloaders without declaring a dependency on them.
     *
     * @param commands the commands being provisioned
     * @param collated the set of contributions being provisioned collated by zone
     */
    private void createExtensionCommands(Map<String, List<CompensatableCommand>> commands, Map<String, List<Contribution>> collated) {
        for (Map.Entry<String, List<Contribution>> entry : collated.entrySet()) {
            String zone = entry.getKey();
            for (Contribution contribution : entry.getValue()) {
                URI contributionUri = contribution.getUri();
                for (URI providerUri : contribution.getResolvedExtensionProviders()) {
                    AttachExtensionCommand command = new AttachExtensionCommand(contributionUri, providerUri);
                    commands.get(zone).add(command);
                }
            }
        }
    }

}
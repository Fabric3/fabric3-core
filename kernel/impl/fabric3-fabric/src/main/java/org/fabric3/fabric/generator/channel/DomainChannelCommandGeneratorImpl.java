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
*/
package org.fabric3.fabric.generator.channel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.channel.ChannelIntents;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class DomainChannelCommandGeneratorImpl implements DomainChannelCommandGenerator {
    private GeneratorRegistry generatorRegistry;

    public DomainChannelCommandGeneratorImpl(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    public BuildChannelsCommand generateBuild(LogicalChannel channel, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = createBuildDefinitions(channel, incremental);
        if (definitions.isEmpty()) {
            return null;
        }
        return new BuildChannelsCommand(definitions);
    }

    public DisposeChannelsCommand generateDispose(LogicalChannel channel, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = createDisposeDefinitions(channel);
        if (definitions.isEmpty()) {
            return null;
        }
        return new DisposeChannelsCommand(definitions);
    }

    private List<PhysicalChannelDefinition> createBuildDefinitions(LogicalChannel channel, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        if (channel.getState() == LogicalState.NEW || !incremental) {
            generateChannelDefinition(channel, definitions);
        }
        return definitions;
    }

    private List<PhysicalChannelDefinition> createDisposeDefinitions(LogicalChannel channel) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        if (channel.getState() == LogicalState.MARKED) {
            generateChannelDefinition(channel, definitions);
        }
        return definitions;
    }

    @SuppressWarnings({"unchecked"})
    private void generateChannelDefinition(LogicalChannel channel, List<PhysicalChannelDefinition> definitions) throws GenerationException {
        URI uri = channel.getUri();
        QName deployable = channel.getDeployable();
        boolean sync = channel.getDefinition().getIntents().contains(ChannelIntents.SYNC_INTENT);
        boolean replicate = channel.getDefinition().getIntents().contains(ChannelIntents.REPLICATE_INTENT);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(uri, deployable, sync, replicate);

        if (!channel.getBindings().isEmpty()) {
            // generate binding information
            LogicalBinding<?> binding = channel.getBinding();
            ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
            PhysicalChannelBindingDefinition bindingDefinition = bindingGenerator.generateChannelBinding(binding);
            definition.setBindingDefinition(bindingDefinition);
        }

        definitions.add(definition);
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}
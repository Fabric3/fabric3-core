/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.fabric.command.UnBuildChannelsCommand;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.spi.command.CompensatableCommand;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class DomainChannelCommandGeneratorImpl extends AbstractChannelCommandGenerator implements DomainChannelCommandGenerator {

    public DomainChannelCommandGeneratorImpl(@Reference GeneratorRegistry generatorRegistry) {
        super(generatorRegistry);
    }

    public CompensatableCommand generateBuild(LogicalChannel channel, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = createBuildDefinitions(channel, incremental);
        if (definitions.isEmpty()) {
            return null;
        }
        return new BuildChannelsCommand(definitions);
    }

    public CompensatableCommand generateUnBuild(LogicalChannel channel, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = createUnBuildDefinitions(channel);
        if (definitions.isEmpty()) {
            return null;
        }
        return new UnBuildChannelsCommand(definitions);
    }

    public ChannelConnectionCommand generateAttachDetach(LogicalChannel channel, boolean incremental) throws GenerationException {
        if (!channel.isConcreteBound()) {
            return null;
        }
        ChannelConnectionCommand connectionCommand = new ChannelConnectionCommand();
        generateDefinitions(channel, connectionCommand, incremental);
        if (connectionCommand.getAttachCommands().isEmpty() && connectionCommand.getDetachCommands().isEmpty()) {
            return null;
        }
        return connectionCommand;
    }

    private List<PhysicalChannelDefinition> createBuildDefinitions(LogicalChannel channel, boolean incremental) {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        if (channel.getState() == LogicalState.NEW || !incremental) {
            generateChannelDefinition(channel, definitions);
        }
        return definitions;
    }


    private List<PhysicalChannelDefinition> createUnBuildDefinitions(LogicalChannel channel) {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        if (channel.getState() == LogicalState.MARKED) {
            generateChannelDefinition(channel, definitions);
        }
        return definitions;
    }

    private void generateChannelDefinition(LogicalChannel channel, List<PhysicalChannelDefinition> definitions) {
        boolean sync = channel.getDefinition().getIntents().contains(ChannelIntents.SYNC_INTENT);
        PhysicalChannelDefinition definition = new PhysicalChannelDefinition(channel.getUri(), channel.getDeployable(), sync);
        definitions.add(definition);
    }
}
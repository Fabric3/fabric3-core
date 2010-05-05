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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.generator.channel;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.builder.channel.ChannelTargetDefinition;
import org.fabric3.fabric.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.fabric.command.DetachChannelConnectionCommand;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalEventStreamDefinition;

/**
 * Base implementation for generating {@link AttachChannelConnectionCommand}s and {@link DetachChannelConnectionCommand}s for logical channel
 * bindings.
 *
 * @version $Revision: 8833 $ $Date: 2010-04-08 13:27:22 +0200 (Thu, 08 Apr 2010) $
 */
@EagerInit
public class AbstractChannelCommandGenerator {
    private GeneratorRegistry generatorRegistry;

    public AbstractChannelCommandGenerator(GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @SuppressWarnings({"unchecked"})
    protected void generateDefinitions(LogicalChannel channel, ChannelConnectionCommand connectionCommand, boolean incremental)
            throws GenerationException {
        for (LogicalBinding<?> binding : channel.getBindings()) {
            ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
            PhysicalConnectionSourceDefinition source = bindingGenerator.generateConnectionSource(binding);
            PhysicalConnectionTargetDefinition target = new ChannelTargetDefinition(channel.getUri());

            URI classLoaderUri = channel.getDefinition().getContributionUri();
            source.setClassLoaderId(classLoaderUri);
            target.setClassLoaderId(classLoaderUri);

            List<PhysicalEventStreamDefinition> streams = generateStreams();
            PhysicalChannelConnectionDefinition definition = new PhysicalChannelConnectionDefinition(source, target, streams);

            if (LogicalState.NEW == channel.getState() || !incremental || LogicalState.NEW == binding.getState()) {
                AttachChannelConnectionCommand attachCommand = new AttachChannelConnectionCommand(definition);
                connectionCommand.add(attachCommand);
            } else if (LogicalState.MARKED == channel.getState() || !incremental || LogicalState.MARKED == binding.getState()) {
                DetachChannelConnectionCommand attachCommand = new DetachChannelConnectionCommand(definition);
                connectionCommand.add(attachCommand);
            }
        }
    }

    private List<PhysicalEventStreamDefinition> generateStreams() {
        // there is only one event stream between a binding and a channel
        List<PhysicalEventStreamDefinition> streams = new ArrayList<PhysicalEventStreamDefinition>();
        PhysicalEventStreamDefinition definition = new PhysicalEventStreamDefinition("default");
        // TODO implement a type system
        definition.addEventType(Object.class.getName());
        streams.add(definition);
        return streams;
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}
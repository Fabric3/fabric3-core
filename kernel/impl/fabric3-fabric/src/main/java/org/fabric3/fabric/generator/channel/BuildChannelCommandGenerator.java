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

import java.util.ArrayList;
import java.util.List;

import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.fabric.generator.GeneratorNotFoundException;
import org.fabric3.fabric.generator.GeneratorRegistry;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.generator.ChannelGenerator;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates a command to build channels defined in a composite on a runtime.
 */
@EagerInit
public class BuildChannelCommandGenerator implements CommandGenerator {

    private int order;
    private ChannelGenerator channelGenerator;
    private GeneratorRegistry generatorRegistry;

    public BuildChannelCommandGenerator(@Property(name = "order") int order,
                                        @Reference ChannelGenerator channelGenerator,
                                        @Reference GeneratorRegistry generatorRegistry) {
        this.order = order;
        this.channelGenerator = channelGenerator;
        this.generatorRegistry = generatorRegistry;
    }

    public int getOrder() {
        return order;
    }

    public BuildChannelsCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (!(component instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
        List<PhysicalChannelDefinition> definitions = createDefinitions(composite, incremental);
        if (definitions.isEmpty()) {
            return null;
        }
        return new BuildChannelsCommand(definitions);
    }

    private List<PhysicalChannelDefinition> createDefinitions(LogicalCompositeComponent composite, boolean incremental) throws GenerationException {
        List<PhysicalChannelDefinition> definitions = new ArrayList<PhysicalChannelDefinition>();
        for (LogicalChannel channel : composite.getChannels()) {
            if (channel.getState() == LogicalState.NEW || !incremental) {
                PhysicalChannelDefinition definition = channelGenerator.generate(channel);
                if (channel.isBound()) {
                    PhysicalChannelBindingDefinition bindingDefinition = generateBinding(channel, definition);
                    // if the channel is bound and no binding definition was generated, the channel may be optimized away
                    if (bindingDefinition == null) {
                        continue;
                    }
                    definition.setBindingDefinition(bindingDefinition);
                }
                definitions.add(definition);
            }
        }
        return definitions;
    }

    @SuppressWarnings({"unchecked"})
    private PhysicalChannelBindingDefinition generateBinding(LogicalChannel channel, PhysicalChannelDefinition definition) throws GenerationException {
        LogicalBinding<?> binding = channel.getBinding();
        ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
        return bindingGenerator.generateChannelBinding(binding);
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }
}

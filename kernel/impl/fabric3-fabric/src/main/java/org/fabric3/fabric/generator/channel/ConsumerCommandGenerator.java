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
package org.fabric3.fabric.generator.channel;

import java.util.List;

import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.command.ChannelConnectionCommand;
import org.fabric3.fabric.command.DetachChannelConnectionCommand;
import org.fabric3.fabric.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 * Generates a command to establish or remove an event channel connection from a consumer.
 *
 * @version $Revision$ $Date$
 */
public class ConsumerCommandGenerator implements CommandGenerator {
    private ConnectionGenerator connectionGenerator;
    private int order;

    public ConsumerCommandGenerator(@Reference ConnectionGenerator connectionGenerator, @Property(name = "order") int order) {
        this.connectionGenerator = connectionGenerator;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @SuppressWarnings("unchecked")
    public ChannelConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent) {
            return null;
        }

        ChannelConnectionCommand command = new ChannelConnectionCommand();

        for (LogicalConsumer consumer : component.getConsumers()) {
            generateCommand(consumer, command, incremental);
        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateCommand(LogicalConsumer consumer, ChannelConnectionCommand command, boolean incremental) throws GenerationException {
        LogicalComponent<?> component = consumer.getParent();
        if (LogicalState.MARKED == component.getState()) {
            List<PhysicalChannelConnectionDefinition> definitions = connectionGenerator.generateConsumer(consumer);
            for (PhysicalChannelConnectionDefinition definition : definitions) {
                DetachChannelConnectionCommand channelConnectionCommand = new DetachChannelConnectionCommand(definition);
                command.add(channelConnectionCommand);
            }
        } else if (LogicalState.NEW == component.getState() || !incremental) {
            List<PhysicalChannelConnectionDefinition> definitions = connectionGenerator.generateConsumer(consumer);
            for (PhysicalChannelConnectionDefinition definition : definitions) {
                AttachChannelConnectionCommand channelConnectionCommand = new AttachChannelConnectionCommand(definition);
                command.add(channelConnectionCommand);
            }
        }
    }

}
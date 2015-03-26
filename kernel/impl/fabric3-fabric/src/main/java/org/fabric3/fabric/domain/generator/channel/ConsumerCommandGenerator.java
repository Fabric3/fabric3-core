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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.fabric3.fabric.container.command.AttachChannelConnectionCommand;
import org.fabric3.fabric.container.command.BuildChannelCommand;
import org.fabric3.fabric.container.command.ChannelConnectionCommand;
import org.fabric3.fabric.container.command.DetachChannelConnectionCommand;
import org.fabric3.fabric.container.command.DisposeChannelCommand;
import org.fabric3.fabric.domain.generator.CommandGenerator;
import org.fabric3.spi.domain.generator.channel.ConnectionGenerator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnection;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.spi.domain.generator.channel.ChannelDirection.CONSUMER;

/**
 * Generates a command to establish or remove an event channel connection from a consumer. Channel build and dispose commands will be generated for consumer
 * sources.
 */
public class ConsumerCommandGenerator implements CommandGenerator<ChannelConnectionCommand> {
    private ConnectionGenerator connectionGenerator;
    private ChannelCommandGenerator channelGenerator;

    public ConsumerCommandGenerator(@Reference ConnectionGenerator connectionGenerator, @Reference ChannelCommandGenerator channelGenerator) {
        this.connectionGenerator = connectionGenerator;
        this.channelGenerator = channelGenerator;
    }

    public int getOrder() {
        return ATTACH;
    }

    @SuppressWarnings("unchecked")
    public Optional<ChannelConnectionCommand> generate(LogicalComponent<?> component) {
        if (component instanceof LogicalCompositeComponent) {
            return Optional.empty();
        }

        ChannelConnectionCommand command = new ChannelConnectionCommand();

        for (LogicalConsumer consumer : component.getConsumers()) {
            generateCommand(consumer, command);
        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(command);
    }

    private void generateCommand(LogicalConsumer consumer, ChannelConnectionCommand command) {
        LogicalComponent<?> component = consumer.getParent();
        QName deployable = consumer.getParent().getDeployable();
        if (LogicalState.MARKED == component.getState()) {
            Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
            for (URI uri : consumer.getSources()) {
                LogicalChannel channel = InvocableGeneratorHelper.getChannelInHierarchy(uri, consumer);
                DisposeChannelCommand disposeCommand = channelGenerator.generateDispose(channel, deployable, CONSUMER);
                command.addDisposeChannelCommand(disposeCommand);
                channels.put(channel, disposeCommand.getChannel().getDeliveryType());
            }
            List<PhysicalChannelConnection> connections = connectionGenerator.generateConsumer(consumer, channels);
            for (PhysicalChannelConnection connection : connections) {
                DetachChannelConnectionCommand connectionCommand = new DetachChannelConnectionCommand(connection);
                command.add(connectionCommand);
            }
        } else if (LogicalState.NEW == component.getState()) {
            Map<LogicalChannel, DeliveryType> channels = new HashMap<>();
            for (URI uri : consumer.getSources()) {
                LogicalChannel channel = InvocableGeneratorHelper.getChannelInHierarchy(uri, consumer);
                BuildChannelCommand buildCommand = channelGenerator.generateBuild(channel, deployable, CONSUMER);
                command.addBuildChannelCommand(buildCommand);
                channels.put(channel, buildCommand.getChannel().getDeliveryType());
            }
            List<PhysicalChannelConnection> connections = connectionGenerator.generateConsumer(consumer, channels);
            for (PhysicalChannelConnection connection : connections) {
                AttachChannelConnectionCommand connectionCommand = new AttachChannelConnectionCommand(connection);
                command.add(connectionCommand);
            }
        }
    }

}
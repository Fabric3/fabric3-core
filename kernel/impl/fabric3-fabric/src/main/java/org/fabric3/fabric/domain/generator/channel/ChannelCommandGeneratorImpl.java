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
 */
package org.fabric3.fabric.domain.generator.channel;

import javax.xml.namespace.QName;

import org.fabric3.fabric.container.command.BuildChannelCommand;
import org.fabric3.fabric.container.command.DisposeChannelCommand;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class ChannelCommandGeneratorImpl implements ChannelCommandGenerator {
    private ChannelGenerator channelGenerator;

    public ChannelCommandGeneratorImpl(@Reference ChannelGenerator channelGenerator) {
        this.channelGenerator = channelGenerator;
    }

    public BuildChannelCommand generateBuild(LogicalChannel channel, QName deployable, ChannelDirection direction) {
        PhysicalChannel physicalChannel = channelGenerator.generate(channel, deployable, direction);
        return new BuildChannelCommand(physicalChannel);
    }

    public DisposeChannelCommand generateDispose(LogicalChannel channel, QName deployable, ChannelDirection direction) {
        PhysicalChannel physicalChannel = channelGenerator.generate(channel, deployable, direction);
        return new DisposeChannelCommand(physicalChannel);
    }

}
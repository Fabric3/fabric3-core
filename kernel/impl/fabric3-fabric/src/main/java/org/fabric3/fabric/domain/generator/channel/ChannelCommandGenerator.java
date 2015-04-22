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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.fabric.container.command.BuildChannelCommand;
import org.fabric3.fabric.container.command.DisposeChannelCommand;
import org.fabric3.spi.model.instance.LogicalChannel;

/**
 * Creates commands to build and dispose channels.
 */
public interface ChannelCommandGenerator {

    /**
     * Generates a build command.
     *
     * @param channel    the channel to build
     * @param deployable the deployable this channel is provisioned for. This may be different than the deployable the channel is defined in as a producer or
     *                   consumer may be connected to a channel from another composite.
     * @return the command
     * @throws Fabric3Exception if a generation error is encountered
     */
    BuildChannelCommand generateBuild(LogicalChannel channel, QName deployable, ChannelDirection direction) throws Fabric3Exception;

    /**
     * Generates an dispose command.
     *
     * @param channel    the channel to remove
     * @param deployable the deployable this channel is provisioned for. This may be different than the deployable the channel is defined in as a producer or
     *                   consumer may be connected to a channel from another composite.
     * @return the command
     * @throws Fabric3Exception if a generation error is encountered
     */
    DisposeChannelCommand generateDispose(LogicalChannel channel, QName deployable, ChannelDirection direction) throws Fabric3Exception;

}
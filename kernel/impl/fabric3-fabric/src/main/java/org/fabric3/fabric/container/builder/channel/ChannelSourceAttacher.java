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
package org.fabric3.fabric.container.builder.channel;

import java.net.URI;

import org.fabric3.fabric.model.physical.ChannelSourceDefinition;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches the source side of a channel connection to a channel.
 */
@EagerInit
public class ChannelSourceAttacher implements SourceConnectionAttacher<ChannelSourceDefinition> {
    private ChannelManager channelManager;

    public ChannelSourceAttacher(@Reference ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public void attach(ChannelSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ContainerException {
        URI uri = source.getUri();
        Channel channel = getChannel(uri, source.getChannelSide());
        URI targetUri = target.getUri();
        channel.subscribe(targetUri, connection);
    }

    public void detach(ChannelSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ContainerException {
        URI uri = source.getUri();
        Channel channel = getChannel(uri, source.getChannelSide());
        URI targetUri = target.getUri();
        channel.unsubscribe(targetUri);
    }

    private Channel getChannel(URI uri, ChannelSide channelSide) throws ContainerException {
        Channel channel = channelManager.getChannel(uri, channelSide);
        if (channel == null) {
            throw new ContainerException("Channel not found");
        }
        return channel;
    }
}
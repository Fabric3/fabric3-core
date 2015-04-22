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
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.builder.ChannelBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.fabric.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelBuilderRegistryImpl implements ChannelBuilderRegistry {
    private ChannelManager channelManager;

    @Reference(required = false)
    protected Map<String, ChannelBuilder> builders = new HashMap<>();

    public ChannelBuilderRegistryImpl(@Reference ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public Channel build(PhysicalChannel physicalChannel) {
        URI uri = physicalChannel.getUri();
        ChannelSide channelSide = physicalChannel.getChannelSide();
        Channel channel = channelManager.getAndIncrementChannel(uri, channelSide);
        if (channel != null) {
            return channel;
        }
        ChannelBuilder builder = getBuilder(physicalChannel);
        channel = builder.build(physicalChannel);
        channelManager.register(channel);
        return channel;
    }

    public void dispose(PhysicalChannel physicalChannel) {
        ChannelBuilder builder = getBuilder(physicalChannel);
        URI uri = physicalChannel.getUri();
        ChannelSide channelSide = physicalChannel.getChannelSide();
        Channel channel = channelManager.getAndDecrementChannel(uri, channelSide);
        if (channelManager.getCount(uri, channelSide) == 0) {
            channelManager.unregister(uri, channelSide);
            builder.dispose(physicalChannel, channel);
        }
    }

    private ChannelBuilder getBuilder(PhysicalChannel definition) {
        ChannelBuilder builder = builders.get(definition.getType());
        if (builder == null) {
            throw new Fabric3Exception("Channel builder not found for type " + definition.getType());
        }
        return builder;
    }

}

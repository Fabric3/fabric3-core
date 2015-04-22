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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.fabric.model.physical.ChannelSource;
import org.fabric3.spi.container.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.fabric.container.channel.ChannelManager;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches the source side of a channel connection to a channel.
 */
@EagerInit
public class ChannelSourceAttacher implements SourceConnectionAttacher<ChannelSource> {
    private ChannelManager channelManager;

    public ChannelSourceAttacher(@Reference ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public void attach(ChannelSource source, PhysicalConnectionTarget target, ChannelConnection connection) throws Fabric3Exception {
        connection.setCloseable(() -> {      // no-op
        });
        if (target.isDirectConnection()) {
            // no event stream to attach since this is a direct connection
            return;
        }

        URI uri = source.getUri();
        Channel channel = getChannel(uri, source.getChannelSide());
        URI targetUri = target.getUri();
        channel.subscribe(targetUri, connection);
    }

    public void detach(ChannelSource source, PhysicalConnectionTarget target) throws Fabric3Exception {
        if (target.isDirectConnection()) {
            // no event stream to attach since this is a direct connection
            return;
        }

        URI uri = source.getUri();
        Channel channel = getChannel(uri, source.getChannelSide());
        URI targetUri = target.getUri();
        channel.unsubscribe(targetUri);
    }

    private Channel getChannel(URI uri, ChannelSide channelSide) throws Fabric3Exception {
        Channel channel = channelManager.getChannel(uri, channelSide);
        if (channel == null) {
            throw new Fabric3Exception("Channel not found");
        }
        return channel;
    }
}
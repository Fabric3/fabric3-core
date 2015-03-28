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
package org.fabric3.channel.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.channel.impl.AsyncFanOutHandler;
import org.fabric3.channel.impl.DefaultChannelImpl;
import org.fabric3.channel.impl.FanOutHandler;
import org.fabric3.channel.impl.SyncFanOutHandler;
import org.fabric3.spi.container.builder.channel.ChannelBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates and disposes default channel implementations.
 */
public class DefaultChannelBuilder implements ChannelBuilder {

    private ExecutorService executorService;

    public DefaultChannelBuilder(@Reference(name = "executorService") ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Channel build(PhysicalChannel physicalChannel) throws Fabric3Exception {
        URI uri = physicalChannel.getUri();
        QName deployable = physicalChannel.getDeployable();

        FanOutHandler fanOutHandler;
        if (physicalChannel.isBound()) {
            // if a binding is set on the channel, make the channel synchronous since async behavior will be provided by the binding
            fanOutHandler = new SyncFanOutHandler();
        } else {
            // the channel is local, have it implement asynchrony
            fanOutHandler = new AsyncFanOutHandler(executorService);
        }

        return new DefaultChannelImpl(uri, deployable, fanOutHandler, physicalChannel.getChannelSide());
    }

    public void dispose(PhysicalChannel physicalChannel, Channel channel) throws Fabric3Exception {
        // no-op
    }

}

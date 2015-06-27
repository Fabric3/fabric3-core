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
package org.fabric3.channel.disruptor.generator;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.spi.domain.generator.ChannelGeneratorExtension;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.oasisopen.sca.annotation.EagerInit;
import static org.fabric3.spi.model.physical.DeliveryType.ASYNCHRONOUS_WORKER;

/**
 *
 */
@EagerInit
public class RingBufferChannelGeneratorExtension implements ChannelGeneratorExtension {

    public PhysicalChannel generate(LogicalChannel channel, URI contributionUri) throws Fabric3Exception {
        URI uri = channel.getUri();
        Channel channelDefinition = channel.getDefinition();
        String channelType = channelDefinition.getType();

        PhysicalChannel physicalChannel = new PhysicalChannel(uri, channelType, ASYNCHRONOUS_WORKER, contributionUri);
        physicalChannel.setMetadata(channelDefinition.getMetadata(Object.class));

        return physicalChannel;
    }

}

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
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ChannelGenerator;
import org.fabric3.spi.domain.generator.channel.ChannelGeneratorExtension;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannel;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelGeneratorImpl implements ChannelGenerator {
    @Reference
    protected Map<String, ChannelGeneratorExtension> extensions;

    @SuppressWarnings("unchecked")
    public PhysicalChannel generate(LogicalChannel channel, QName deployable, ChannelDirection direction) {

        String type = channel.getDefinition().getType();
        ChannelGeneratorExtension generator = extensions.get(type);
        if (generator == null) {
            throw new Fabric3Exception("Channel generator not found: " + type);
        }
        PhysicalChannel physicalChannel = generator.generate(channel, deployable);
        if (!channel.getBindings().isEmpty()) {
            // generate binding information
            physicalChannel.setBound(true);
            physicalChannel.setChannelSide(ChannelDirection.CONSUMER == direction ? ChannelSide.CONSUMER : ChannelSide.PRODUCER);
        } else {
            physicalChannel.setChannelSide(ChannelSide.COLLOCATED);
        }
        return physicalChannel;
    }

}

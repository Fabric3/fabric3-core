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
package org.fabric3.spi.domain.generator.channel;

import javax.xml.namespace.QName;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * Generates a physical channel definition for the logical channel.
 */
public interface ChannelGenerator {

    /**
     * Generate the definition.
     *
     * @param channel    the logical channel
     * @param deployable the deployable the channel is contained in
     * @param direction  whether the channel will connect a consumer or producer
     * @return the definition
     * @throws Fabric3Exception if there is a generation error
     */
    PhysicalChannelDefinition generateChannelDefinition(LogicalChannel channel, QName deployable, ChannelDirection direction) throws Fabric3Exception;
}

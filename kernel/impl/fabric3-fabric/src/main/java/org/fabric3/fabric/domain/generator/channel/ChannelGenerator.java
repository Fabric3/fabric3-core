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

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 * Generates a physical channel for the logical channel.
 */
public interface ChannelGenerator {

    /**
     * Generate the physical channel.
     *
     * @param channel         the logical channel
     * @param contributionUri the contribution URI the channel is deployed for
     * @param direction       whether the channel will connect a consumer or producer
     * @return the physical channel
     * @throws Fabric3Exception if there is a generation error
     */
    PhysicalChannel generate(LogicalChannel channel, URI contributionUri, ChannelDirection direction) throws Fabric3Exception;
}

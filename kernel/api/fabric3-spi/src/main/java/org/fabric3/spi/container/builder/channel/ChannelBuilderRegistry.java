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
package org.fabric3.spi.container.builder.channel;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 * Builds a channel by dispatching to a {@link ChannelBuilder}
 */
public interface ChannelBuilderRegistry {

    /**
     * Creates the channel from the physical channel.
     *
     * @param physicalChannel the physical channel definition
     * @return the channel
     * @throws Fabric3Exception if there is an error building the channel
     */
    Channel build(PhysicalChannel physicalChannel) throws Fabric3Exception;

    /**
     * Disposes a channel.
     *
     * @param physicalChannel the physical channel physical channel
     * @throws Fabric3Exception if there is an error disposing the channel
     */
    void dispose(PhysicalChannel physicalChannel) throws Fabric3Exception;
}

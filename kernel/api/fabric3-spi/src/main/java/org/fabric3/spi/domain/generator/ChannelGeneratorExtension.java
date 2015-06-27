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
package org.fabric3.spi.domain.generator;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.PhysicalChannel;

/**
 * Generates metadata used to provision a channel to a runtime.
 */
public interface ChannelGeneratorExtension {

    /**
     * Generates a {@link PhysicalChannel} for the channel.
     *
     * @param channel         the logical channel to generate
     * @param contributionUri the contribution this channel is being provisioned under. This may be different than the contribution where the channel is
     *                        defined, e .g. if the channel is provisioned for a producer or consumer in another deployable
     * @return the physical channel definition
     * @throws Fabric3Exception if there is an error generating the channel
     */
    PhysicalChannel generate(LogicalChannel channel, URI contributionUri) throws Fabric3Exception;
}

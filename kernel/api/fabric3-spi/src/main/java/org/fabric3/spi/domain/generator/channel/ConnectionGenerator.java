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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.domain.generator.channel;

import java.util.List;
import java.util.Map;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelConnectionDefinition;

/**
 * Generates physical metadata for an event channel connection.
 */
public interface ConnectionGenerator {

    /**
     * Generate event channel connection metadata from a logical producer.
     *
     * @param producer the logical producer
     * @param channels the a map of channels and delivery semantics the consumer is connected to
     * @return the event channel connection metadata
     * @throws Fabric3Exception if a generation error is encountered
     */
    List<PhysicalChannelConnectionDefinition> generateProducer(LogicalProducer producer, Map<LogicalChannel, DeliveryType> channels)
            throws Fabric3Exception;

    /**
     * Generate event channel connection metadata from a logical consumer.
     *
     * @param consumer the logical consumer
     * @param channels the a map of channels and delivery semantics the consumer is connected to
     * @return the event channel connection metadata
     * @throws Fabric3Exception if a generation error is encountered
     */
    List<PhysicalChannelConnectionDefinition> generateConsumer(LogicalConsumer consumer, Map<LogicalChannel, DeliveryType> channels)
            throws Fabric3Exception;
}
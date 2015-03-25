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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.domain.generator.channel;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generates {@link PhysicalConnectionSourceDefinition}s and {@link PhysicalConnectionTargetDefinition}s for resolved connection bindings.
 */
public interface ConnectionBindingGenerator<BD extends Binding> {

    /**
     * Generates metadata used to provision a binding transport when a channel is deployed. If provisioning is not required, this method may return null.
     *
     * @param binding      the channel binding configuration
     * @param deliveryType the delivery type implemented by the channel this binding will connect to
     * @return the binding transport metadata or null if provisioning is not required
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<BD> binding, DeliveryType deliveryType) throws Fabric3Exception;

    /**
     * Generates metadata used to attach a consumer to a channel binding transport.
     *
     * @param consumer     rhe consumer
     * @param binding      the channel binding configuration
     * @param deliveryType the channel delivery semantics
     * @return the connection metadata
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer, LogicalBinding<BD> binding, DeliveryType deliveryType)
            throws Fabric3Exception;

    /**
     * Generates metadata used to attach a producer to a channel binding transport.
     *
     * @param producer     the producer
     * @param binding      the channel binding configuration
     * @param deliveryType the channel delivery semantics
     * @return the connection metadata
     * @throws Fabric3Exception if an error occurs during the generation process
     */
    PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer, LogicalBinding<BD> binding, DeliveryType deliveryType)
            throws Fabric3Exception;

}
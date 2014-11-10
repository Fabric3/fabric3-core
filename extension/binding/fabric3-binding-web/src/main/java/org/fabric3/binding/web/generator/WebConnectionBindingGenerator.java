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
package org.fabric3.binding.web.generator;

import java.net.URI;

import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.binding.web.provision.WebChannelBindingDefinition;
import org.fabric3.binding.web.provision.WebConnectionSourceDefinition;
import org.fabric3.binding.web.provision.WebConnectionTargetDefinition;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 * Generates a {@link PhysicalConnectionSourceDefinition} for attaching a channel to a websocket or comet connection.
 */
@EagerInit
public class WebConnectionBindingGenerator implements ConnectionBindingGenerator<WebBindingDefinition> {

    public PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<WebBindingDefinition> binding, ChannelDeliveryType deliveryType)
            throws GenerationException {
        OperationsAllowed allowed = binding.getDefinition().getAllowed();
        return new WebChannelBindingDefinition(allowed);
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer,
                                                                       LogicalBinding<WebBindingDefinition> binding,
                                                                       ChannelDeliveryType deliveryType) {
        URI channelUri = binding.getParent().getUri();
        return new WebConnectionSourceDefinition(consumer.getUri(), channelUri);
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer,
                                                                       LogicalBinding<WebBindingDefinition> binding,
                                                                       ChannelDeliveryType deliveryType) {
        WebConnectionTargetDefinition definition = new WebConnectionTargetDefinition();
        URI channelUri = binding.getParent().getUri();
        definition.setUri(channelUri);
        return definition;
    }

}
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
package org.fabric3.binding.zeromq.generator;

import java.net.URI;

import org.fabric3.binding.zeromq.provision.ZeroMQChannelBindingDefinition;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionSourceDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionTargetDefinition;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;

/**
 *
 */
@EagerInit
public class ZeroMQConnectionBindingGenerator implements ConnectionBindingGenerator<ZeroMQBinding> {

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer,
                                                                       LogicalBinding<ZeroMQBinding> binding,
                                                                       ChannelDeliveryType deliveryType) throws GenerationException {
        URI uri = consumer.getUri();

        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        setChannelName(binding, metadata);
        return new ZeroMQConnectionSourceDefinition(uri, metadata);

    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer,
                                                                       LogicalBinding<ZeroMQBinding> binding,
                                                                       ChannelDeliveryType deliveryType) throws GenerationException {
        ZeroMQBinding bindingDefinition = binding.getDefinition();
        URI targetUri = bindingDefinition.getTargetUri();
        if (targetUri == null) {
            // no target uri specified, generate one as it is not used for the endpoint address
            targetUri = binding.getParent().getUri();
        }
        ZeroMQMetadata metadata = bindingDefinition.getZeroMQMetadata();
        setChannelName(binding, metadata);
        boolean dedicatedThread = ChannelDeliveryType.ASYNCHRONOUS_WORKER == deliveryType;

        return new ZeroMQConnectionTargetDefinition(targetUri, metadata, dedicatedThread);
    }

    public PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<ZeroMQBinding> binding, ChannelDeliveryType deliveryType)
            throws GenerationException {
        return new ZeroMQChannelBindingDefinition(deliveryType);
    }

    private void setChannelName(LogicalBinding binding, ZeroMQMetadata metadata) {
        if (binding.getParent() instanceof LogicalChannel) {
            String channelName = ((LogicalChannel) binding.getParent()).getDefinition().getName();
            metadata.setChannelName(channelName);
        }
    }

}

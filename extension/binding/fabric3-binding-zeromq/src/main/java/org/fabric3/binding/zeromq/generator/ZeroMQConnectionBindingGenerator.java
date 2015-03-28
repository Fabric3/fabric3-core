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

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionSource;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionTarget;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.physical.DeliveryType;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
@Key("org.fabric3.api.binding.zeromq.model.ZeroMQBinding")
public class ZeroMQConnectionBindingGenerator implements ConnectionBindingGenerator<ZeroMQBinding> {

    public PhysicalConnectionSource generateConnectionSource(LogicalConsumer consumer, LogicalBinding<ZeroMQBinding> binding, DeliveryType deliveryType) {
        URI uri = consumer.getUri();
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        setChannelName(binding, metadata);
        return new ZeroMQConnectionSource(uri, metadata);
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalProducer producer, LogicalBinding<ZeroMQBinding> binding, DeliveryType deliveryType) {
        ZeroMQBinding bindingDefinition = binding.getDefinition();
        URI targetUri = bindingDefinition.getTargetUri();
        if (targetUri == null) {
            // no target uri specified, generate one as it is not used for the endpoint address
            targetUri = binding.getParent().getUri();
        }
        ZeroMQMetadata metadata = bindingDefinition.getZeroMQMetadata();
        setChannelName(binding, metadata);
        boolean dedicatedThread = DeliveryType.ASYNCHRONOUS_WORKER == deliveryType;

        return new ZeroMQConnectionTarget(targetUri, metadata, dedicatedThread);
    }

    private void setChannelName(LogicalBinding binding, ZeroMQMetadata metadata) {
        if (binding.getParent() instanceof LogicalChannel) {
            String channelName = ((LogicalChannel) binding.getParent()).getDefinition().getName();
            metadata.setChannelName(channelName);
        }
    }

}

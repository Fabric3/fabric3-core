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

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.fabric.domain.generator.GeneratorRegistry;
import org.fabric3.spi.domain.generator.channel.ChannelDirection;
import org.fabric3.spi.domain.generator.channel.ChannelGenerator;
import org.fabric3.spi.domain.generator.channel.ChannelGeneratorExtension;
import org.fabric3.spi.domain.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ChannelGeneratorImpl implements ChannelGenerator {
    private Map<String, ChannelGeneratorExtension> extensions;
    private GeneratorRegistry generatorRegistry;

    @Reference
    public void setExtensions(Map<String, ChannelGeneratorExtension> extensions) {
        this.extensions = extensions;
    }

    public ChannelGeneratorImpl(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @SuppressWarnings("unchecked")
    public PhysicalChannelDefinition generateChannelDefinition(LogicalChannel channel, QName deployable, ChannelDirection direction)
            throws ContainerException {

        LogicalBinding<?> binding = channel.getBinding();
        String type = channel.getDefinition().getType();
        ChannelGeneratorExtension generator = extensions.get(type);
        if (generator == null) {
            throw new ContainerException("Channel generator not found: " + type);
        }
        PhysicalChannelDefinition definition = generator.generate(channel, deployable);
        if (!channel.getBindings().isEmpty()) {
            // generate binding information
            ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
            ChannelDeliveryType deliveryType = definition.getDeliveryType();
            PhysicalChannelBindingDefinition bindingDefinition = bindingGenerator.generateChannelBinding(binding, deliveryType);
            definition.setBindingDefinition(bindingDefinition);
            definition.setChannelSide(ChannelDirection.CONSUMER == direction ? ChannelSide.CONSUMER : ChannelSide.PRODUCER);
        } else {
            definition.setChannelSide(ChannelSide.COLLOCATED);
        }
        return definition;
    }

    @SuppressWarnings("unchecked")
    private <T extends Binding> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws ContainerException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}

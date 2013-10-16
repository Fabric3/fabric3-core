/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.deployment.generator.channel;

import javax.xml.namespace.QName;
import java.util.Map;

import org.fabric3.fabric.deployment.generator.GeneratorNotFoundException;
import org.fabric3.fabric.deployment.generator.GeneratorRegistry;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.spi.deployment.generator.channel.ChannelDirection;
import org.fabric3.spi.deployment.generator.channel.ChannelGenerator;
import org.fabric3.spi.deployment.generator.channel.ChannelGeneratorExtension;
import org.fabric3.spi.deployment.generator.channel.ConnectionBindingGenerator;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.physical.ChannelDeliveryType;
import org.fabric3.spi.model.physical.ChannelSide;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.fabric3.spi.model.type.binding.SCABinding;
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
            throws GenerationException {

        LogicalBinding<?> binding = channel.getBinding();
        String type = channel.getDefinition().getType();
        ChannelGeneratorExtension generator = extensions.get(type);
        if (generator == null) {
            throw new GenerationException("Channel generator not found: " + type);
        }
        PhysicalChannelDefinition definition = generator.generate(channel, deployable);
        if (!channel.getBindings().isEmpty()) {
            // generate binding information
            if (!(binding.getDefinition() instanceof SCABinding)) {
                // avoid generating SCABinding
                ConnectionBindingGenerator bindingGenerator = getGenerator(binding);
                ChannelDeliveryType deliveryType = definition.getDeliveryType();
                PhysicalChannelBindingDefinition bindingDefinition = bindingGenerator.generateChannelBinding(binding, deliveryType);
                definition.setBindingDefinition(bindingDefinition);
                definition.setChannelSide(ChannelDirection.CONSUMER == direction ? ChannelSide.CONSUMER : ChannelSide.PRODUCER);
            } else {
                definition.setChannelSide(ChannelSide.COLLOCATED);
            }
        } else {
            definition.setChannelSide(ChannelSide.COLLOCATED);
        }
        return definition;
    }

    @SuppressWarnings("unchecked")
    private <T extends BindingDefinition> ConnectionBindingGenerator<T> getGenerator(LogicalBinding<T> binding) throws GeneratorNotFoundException {
        return (ConnectionBindingGenerator<T>) generatorRegistry.getConnectionBindingGenerator(binding.getDefinition().getClass());
    }

}

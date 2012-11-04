/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.generator;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionSourceDefinition;
import org.fabric3.binding.zeromq.provision.ZeroMQConnectionTargetDefinition;
import org.fabric3.spi.generator.ConnectionBindingGenerator;
import org.fabric3.spi.generator.GenerationException;
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
public class ZeroMQConnectionBindingGenerator implements ConnectionBindingGenerator<ZeroMQBindingDefinition> {

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalConsumer consumer, LogicalBinding<ZeroMQBindingDefinition> binding)
            throws GenerationException {
        URI uri = consumer.getUri();
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        setChannelName(binding, metadata);
        return new ZeroMQConnectionSourceDefinition(uri, metadata);

    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalProducer producer, LogicalBinding<ZeroMQBindingDefinition> binding)
            throws GenerationException {
        URI targetUri = binding.getDefinition().getTargetUri();
        ZeroMQMetadata metadata = binding.getDefinition().getZeroMQMetadata();
        setChannelName(binding, metadata);
        return new ZeroMQConnectionTargetDefinition(targetUri, metadata);
    }

    public PhysicalChannelBindingDefinition generateChannelBinding(LogicalBinding<ZeroMQBindingDefinition> binding) throws GenerationException {
        // do nothing
        return null;
    }

    private void setChannelName(LogicalBinding binding, ZeroMQMetadata metadata) {
        if (binding.getParent() instanceof LogicalChannel) {
            String channelName = ((LogicalChannel) binding.getParent()).getDefinition().getName();
            metadata.setChannelName(channelName);
        }
    }

}

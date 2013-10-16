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
package org.fabric3.fabric.deployment.instantiator.channel;

import java.net.URI;

import org.fabric3.fabric.deployment.instantiator.ChannelInstantiator;
import org.fabric3.fabric.deployment.instantiator.InstantiationContext;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ChannelDefinition;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Default implementation of ChannelInstantiator.
 */
public class ChannelInstantiatorImpl implements ChannelInstantiator {

    public void instantiateChannels(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        for (ChannelDefinition definition : composite.getChannels().values()) {
            URI uri = URI.create(parent.getUri() + "/" + definition.getName());
            if (parent.getChannel(uri) != null) {
                DuplicateChannel error = new DuplicateChannel(uri, parent);
                context.addError(error);
                continue;
            }
            LogicalChannel channel = new LogicalChannel(uri, definition, parent);
            for (BindingDefinition binding : definition.getBindings()) {
                LogicalBinding<BindingDefinition> logicalBinding = new LogicalBinding<BindingDefinition>(binding, channel);
                channel.addBinding(logicalBinding);
            }
            channel.setDeployable(composite.getName());
            parent.addChannel(channel);
        }
    }

}
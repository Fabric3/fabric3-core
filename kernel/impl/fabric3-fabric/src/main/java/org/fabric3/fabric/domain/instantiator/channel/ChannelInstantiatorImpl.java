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
package org.fabric3.fabric.domain.instantiator.channel;

import java.net.URI;

import org.fabric3.fabric.domain.instantiator.ChannelInstantiator;
import org.fabric3.fabric.domain.instantiator.InstantiationContext;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Default implementation of ChannelInstantiator.
 */
public class ChannelInstantiatorImpl implements ChannelInstantiator {

    public void instantiateChannels(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        for (Channel definition : composite.getChannels().values()) {
            URI uri = URI.create(parent.getUri() + "/" + definition.getName());
            if (parent.getChannel(uri) != null) {
                DuplicateChannel error = new DuplicateChannel(uri, parent);
                context.addError(error);
                continue;
            }
            LogicalChannel channel = new LogicalChannel(uri, definition, parent);
            for (Binding binding : definition.getBindings()) {
                LogicalBinding<Binding> logicalBinding = new LogicalBinding<>(binding, channel);
                channel.addBinding(logicalBinding);
            }
            channel.setDeployable(composite.getName());
            parent.addChannel(channel);
        }
    }

}
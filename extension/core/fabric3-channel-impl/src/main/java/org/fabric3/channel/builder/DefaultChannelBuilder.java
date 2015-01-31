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
package org.fabric3.channel.builder;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.channel.impl.AsyncFanOutHandler;
import org.fabric3.channel.impl.DefaultChannelImpl;
import org.fabric3.channel.impl.FanOutHandler;
import org.fabric3.channel.impl.SyncFanOutHandler;
import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.channel.ChannelBuilder;
import org.fabric3.spi.container.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.container.channel.Channel;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates and disposes default channel implementations.
 */
public class DefaultChannelBuilder implements ChannelBuilder {

    private ExecutorService executorService;

    private Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder> bindingBuilders = Collections.emptyMap();

    public DefaultChannelBuilder(@Reference(name = "executorService") ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Reference(required = false)
    public void setBindingBuilders(Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder> builders) {
        this.bindingBuilders = builders;
    }

    public Channel build(PhysicalChannelDefinition definition) throws ContainerException {
        URI uri = definition.getUri();
        QName deployable = definition.getDeployable();

        FanOutHandler fanOutHandler;
        if (definition.getBindingDefinition() != null) {
            // if a binding is set on the channel, make the channel is synchronous since async behavior will be provided by the binding
            fanOutHandler = new SyncFanOutHandler();
        } else {
            // the channel is local, have it implement asynchrony
            fanOutHandler = new AsyncFanOutHandler(executorService);
        }

        Channel channel = new DefaultChannelImpl(uri, deployable, fanOutHandler, definition.getChannelSide());
        PhysicalChannelBindingDefinition bindingDefinition = definition.getBindingDefinition();
        buildBinding(channel, bindingDefinition);

        return channel;
    }

    public void dispose(PhysicalChannelDefinition definition, Channel channel) throws ContainerException {
        disposeBinding(channel, definition.getBindingDefinition());
    }

    @SuppressWarnings({"unchecked"})
    private void buildBinding(Channel channel, PhysicalChannelBindingDefinition bindingDefinition) throws ContainerException {
        if (bindingDefinition != null) {
            ChannelBindingBuilder builder = getBuilder(bindingDefinition);
            builder.build(bindingDefinition, channel);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void disposeBinding(Channel channel, PhysicalChannelBindingDefinition bindingDefinition) throws ContainerException {
        if (bindingDefinition != null) {
            ChannelBindingBuilder builder = getBuilder(bindingDefinition);
            builder.dispose(bindingDefinition, channel);
        }
    }

    private ChannelBindingBuilder getBuilder(PhysicalChannelBindingDefinition definition) throws ContainerException {
        ChannelBindingBuilder<?> builder = bindingBuilders.get(definition.getClass());
        if (builder == null) {
            throw new ContainerException("Channel binding builder not found for type " + definition.getClass());
        }
        return builder;
    }

}

/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.executor;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.RegistrationException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * Removes a set of channels defined in a composite on a runtime.
 */
@EagerInit
public class DisposeChannelsCommandExecutor implements CommandExecutor<DisposeChannelsCommand> {
    private ChannelManager channelManager;
    private CommandExecutorRegistry executorRegistry;
    private ZoneTopologyService topologyService;
    private boolean replicationCapable;

    private Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>>
            builders = Collections.emptyMap();

    @Reference(required = false)
    public void setTopologyService(List<ZoneTopologyService> services) {
        // use a collection to force reinjection
        if (services != null && !services.isEmpty()) {
            this.topologyService = services.get(0);
            replicationCapable = topologyService.supportsDynamicChannels();
        }
    }

    @Reference(required = false)
    public void setBuilders(Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>> builders) {
        this.builders = builders;
    }

    @Constructor
    public DisposeChannelsCommandExecutor(@Reference ChannelManager channelManager, @Reference CommandExecutorRegistry executorRegistry) {
        this.channelManager = channelManager;
        this.executorRegistry = executorRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(DisposeChannelsCommand.class, this);
    }

    public void execute(DisposeChannelsCommand command) throws ExecutionException {
        try {
            List<PhysicalChannelDefinition> definitions = command.getDefinitions();
            for (PhysicalChannelDefinition definition : definitions) {
                URI uri = definition.getUri();
                Channel channel = channelManager.unregister(uri);

                if (definition.isReplicate() && replicationCapable) {
                    String channelName = uri.toString();
                    try {
                        topologyService.closeChannel(channelName);
                    } catch (ZoneChannelException e) {
                        throw new ExecutionException(e);
                    }
                }
                PhysicalChannelBindingDefinition bindingDefinition = definition.getBindingDefinition();
                disposeBinding(channel, bindingDefinition);

            }
        } catch (RegistrationException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void disposeBinding(Channel channel, PhysicalChannelBindingDefinition bindingDefinition) throws ExecutionException {
        if (bindingDefinition != null) {
            ChannelBindingBuilder builder = getBuilder(bindingDefinition);
            try {
                builder.dispose(bindingDefinition, channel);
            } catch (BuilderException e) {
                throw new ExecutionException(e);
            }
        }
    }

    private ChannelBindingBuilder getBuilder(PhysicalChannelBindingDefinition bindingDefinition) throws ExecutionException {
        ChannelBindingBuilder<?> builder = builders.get(bindingDefinition.getClass());
        if (builder == null) {
            throw new ExecutionException("Channel binding builder not found for type " + bindingDefinition.getClass());
        }
        return builder;
    }

}
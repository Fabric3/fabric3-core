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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.fabric3.fabric.command.BuildChannelsCommand;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.channel.ChannelBuilder;
import org.fabric3.spi.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.channel.Channel;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.RegistrationException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.model.physical.PhysicalChannelBindingDefinition;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Builds a set of channels defined in a composite on a runtime.
 */
@EagerInit
public class BuildChannelsCommandExecutor implements CommandExecutor<BuildChannelsCommand> {
    private ChannelManager channelManager;
    private CommandExecutorRegistry executorRegistry;

    private Map<Class<? extends PhysicalChannelDefinition>, ChannelBuilder> channelBuilders = Collections.emptyMap();

    private Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>> bindingBuilders
            = Collections.emptyMap();

    @Constructor
    public BuildChannelsCommandExecutor(@Reference ChannelManager channelManager,
                                        @Reference ExecutorService executorService,
                                        @Reference CommandExecutorRegistry executorRegistry) {
        this.channelManager = channelManager;
        this.executorRegistry = executorRegistry;
    }

    @Reference(required = false)
    public void setBindingBuilders(Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>>
                                               builders) {
        this.bindingBuilders = builders;
    }

    @Reference(required = false)
    public void setChannelBuilders(Map<Class<? extends PhysicalChannelDefinition>, ChannelBuilder> channelBuilders) {
        this.channelBuilders = channelBuilders;
    }

    @Init
    public void init() {
        executorRegistry.register(BuildChannelsCommand.class, this);
    }

    public void execute(BuildChannelsCommand command) throws ExecutionException {
        try {
            List<PhysicalChannelDefinition> definitions = command.getDefinitions();
            for (PhysicalChannelDefinition definition : definitions) {

                Channel channel = getBuilder(definition).build(definition);

                PhysicalChannelBindingDefinition bindingDefinition = definition.getBindingDefinition();
                buildBinding(channel, bindingDefinition);
                channelManager.register(channel);
            }
        } catch (RegistrationException e) {
            throw new ExecutionException(e.getMessage(), e);
        } catch (BuilderException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void buildBinding(Channel channel, PhysicalChannelBindingDefinition bindingDefinition) throws ExecutionException {
        if (bindingDefinition != null) {
            ChannelBindingBuilder builder = getBuilder(bindingDefinition);
            try {
                builder.build(bindingDefinition, channel);
            } catch (BuilderException e) {
                throw new ExecutionException(e);
            }
        }
    }

    private ChannelBindingBuilder getBuilder(PhysicalChannelBindingDefinition definition) throws ExecutionException {
        ChannelBindingBuilder<?> builder = bindingBuilders.get(definition.getClass());
        if (builder == null) {
            throw new ExecutionException("Channel binding builder not found for type " + definition.getClass());
        }
        return builder;
    }

    private ChannelBuilder getBuilder(PhysicalChannelDefinition definition) throws ExecutionException {
        ChannelBuilder builder = channelBuilders.get(definition.getClass());
        if (builder == null) {
            throw new ExecutionException("Channel builder not found for type " + definition.getClass());
        }
        return builder;
    }

}
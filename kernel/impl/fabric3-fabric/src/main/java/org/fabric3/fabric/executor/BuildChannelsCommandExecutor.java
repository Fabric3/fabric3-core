/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
import java.util.concurrent.ExecutorService;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.channel.AsyncFanOutHandler;
import org.fabric3.fabric.channel.ChannelImpl;
import org.fabric3.fabric.channel.FanOutHandler;
import org.fabric3.fabric.channel.ReplicationHandler;
import org.fabric3.fabric.channel.SyncFanOutHandler;
import org.fabric3.fabric.command.BuildChannelsCommand;
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
 * Builds a set of channels defined in a composite on a runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class BuildChannelsCommandExecutor implements CommandExecutor<BuildChannelsCommand> {
    private ChannelManager channelManager;
    private ExecutorService executorService;
    private CommandExecutorRegistry executorRegistry;
    private ZoneTopologyService topologyService;
    private boolean replicationCapable;
    private Map<Class<? extends PhysicalChannelBindingDefinition>, ChannelBindingBuilder<? extends PhysicalChannelBindingDefinition>>
            builders = Collections.emptyMap();

    @Constructor
    public BuildChannelsCommandExecutor(@Reference ChannelManager channelManager,
                                        @Reference ExecutorService executorService,
                                        @Reference CommandExecutorRegistry executorRegistry) {
        this.channelManager = channelManager;
        this.executorService = executorService;
        this.executorRegistry = executorRegistry;
    }

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

    @Init
    public void init() {
        executorRegistry.register(BuildChannelsCommand.class, this);
    }

    public void execute(BuildChannelsCommand command) throws ExecutionException {
        try {
            List<PhysicalChannelDefinition> definitions = command.getDefinitions();
            for (PhysicalChannelDefinition definition : definitions) {
                URI uri = definition.getUri();
                QName deployable = definition.getDeployable();
                FanOutHandler fanOutHandler;
                if (definition.isSynchronous()) {
                    fanOutHandler = new SyncFanOutHandler();
                } else {
                    fanOutHandler = new AsyncFanOutHandler(executorService);
                }
                Channel channel;
                if (definition.isReplicate() && replicationCapable) {
                    String channelName = uri.toString();
                    ReplicationHandler replicationHandler = new ReplicationHandler(channelName, topologyService);
                    channel = new ChannelImpl(uri, deployable, replicationHandler, fanOutHandler);
                    try {
                        topologyService.openChannel(channelName, null, replicationHandler);
                    } catch (ZoneChannelException e) {
                        throw new ExecutionException(e);
                    }
                } else {
                    channel = new ChannelImpl(uri, deployable, fanOutHandler);
                }
                PhysicalChannelBindingDefinition bindingDefinition = definition.getBindingDefinition();
                buildBinding(channel, bindingDefinition);
                channelManager.register(channel);
            }
        } catch (RegistrationException e) {
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

    private ChannelBindingBuilder getBuilder(PhysicalChannelBindingDefinition bindingDefinition) throws ExecutionException {
        ChannelBindingBuilder<?> builder = builders.get(bindingDefinition.getClass());
        if (builder == null) {
            throw new ExecutionException("Channel binding builder not found for type " + bindingDefinition.getClass());
        }
        return builder;
    }

}
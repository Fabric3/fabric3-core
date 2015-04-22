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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.command;

import javax.xml.namespace.QName;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.fabric.container.channel.ChannelManager;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.container.component.ScopeRegistry;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.annotation.Constructor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Starts a component context on a runtime.
 */
@EagerInit
public class StartContextCommandExecutor implements CommandExecutor<StartContextCommand> {
    private ScopeContainer compositeScopeContainer;
    private ScopeContainer domainScopeContainer;
    private CommandExecutorRegistry commandExecutorRegistry;
    private ChannelManager channelManager;
    private ContextMonitor monitor;

    @Constructor
    public StartContextCommandExecutor(@Reference CommandExecutorRegistry executorRegistry,
                                       @Reference ScopeRegistry scopeRegistry,
                                       @Reference ChannelManager channelManager,
                                       @Monitor ContextMonitor monitor) {
        this.commandExecutorRegistry = executorRegistry;
        this.channelManager = channelManager;
        this.compositeScopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        this.domainScopeContainer = scopeRegistry.getScopeContainer(Scope.DOMAIN);
        this.monitor = monitor;
    }

    public StartContextCommandExecutor(ScopeRegistry scopeRegistry, @Monitor ContextMonitor monitor) {
        this(null, scopeRegistry, null, monitor);
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(StartContextCommand.class, this);
    }

    public void execute(StartContextCommand command) {
        QName deployable = command.getDeployable();
        WorkContextCache.getAndResetThreadWorkContext();
        // Channels must be started before components since the latter may send events during initialization.
        // See https://fabric3.atlassian.net/browse/FABRIC-10
        if (channelManager != null) {
            channelManager.startContext(deployable);
        }
        compositeScopeContainer.startContext(deployable);
        if (domainScopeContainer != null) {
            // domain scope not available during bootstrap
            domainScopeContainer.startContext(deployable);
        }
        if (command.isLog()) {
            monitor.deployed(deployable);
        }
    }

}
